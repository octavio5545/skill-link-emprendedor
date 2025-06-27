package com.example.demo.postInteractions.service;

import com.example.demo.postInteractions.controller.WebSocketMessageController;
import com.example.demo.postInteractions.dto.CommentDTO;
import com.example.demo.postInteractions.dto.UserDTO;
import com.example.demo.postInteractions.model.Comment;
import com.example.demo.postInteractions.model.Post;
import com.example.demo.postInteractions.model.TargetType;
import com.example.demo.user.model.User;
import com.example.demo.postInteractions.repository.CommentRepository;
import com.example.demo.postInteractions.repository.PostRepository;
import com.example.demo.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final WebSocketMessageController webSocketMessageController;
    private final ReactionService reactionService;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          PostRepository postRepository,
                          WebSocketMessageController webSocketMessageController,
                          ReactionService reactionService) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.webSocketMessageController = webSocketMessageController;
        this.reactionService = reactionService;
    }

    private CommentDTO convertToDto(Comment comment, Long currentUserId,
                                    Map<Long, Map<String, Long>> batchReactionCounts,
                                    Map<Long, String> batchUserReactions,
                                    Map<Long, User> batchUsers) {

        if (comment == null) {
            return null;
        }

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId().toString());
        commentDTO.setContent(comment.getContenido());
        commentDTO.setCreatedAt(comment.getFechaComentario());

        if (batchUsers != null && batchUsers.containsKey(comment.getUser().getId())) {
            User user = batchUsers.get(comment.getUser().getId());
            commentDTO.setAuthor(new UserDTO(user));
        } else {
            commentDTO.setAuthor(new UserDTO(comment.getUser()));
        }

        if (comment.getParentComment() != null) {
            commentDTO.setParentCommentId(comment.getParentComment().getId().toString());
        }

        if (batchReactionCounts != null && batchReactionCounts.containsKey(comment.getId())) {
            Map<String, Long> reactionsLong = batchReactionCounts.get(comment.getId());
            Map<String, Integer> reactionsWithCounts = reactionsLong.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));

            if (!reactionsWithCounts.isEmpty()) {
                commentDTO.setReactions(reactionsWithCounts);
            }
        } else {
            Map<String, Long> reactionsLong = reactionService.getReactionsCountForTarget(comment.getId(), TargetType.COMMENT);
            Map<String, Integer> reactionsWithCounts = reactionsLong.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));

            if (!reactionsWithCounts.isEmpty()) {
                commentDTO.setReactions(reactionsWithCounts);
            }
        }

        if (currentUserId != null) {
            if (batchUserReactions != null) {
                String userReaction = batchUserReactions.get(comment.getId());
                if (userReaction != null) {
                    commentDTO.setUserReaction(userReaction);
                }
            } else {
                String userReaction = reactionService.getUserReactionForTarget(currentUserId, comment.getId(), TargetType.COMMENT);
                if (userReaction != null) {
                    commentDTO.setUserReaction(userReaction);
                }
            }
        }

        return commentDTO;
    }

    private void collectAllCommentIds(List<Comment> comments, Set<Long> allIds) {
        for (Comment comment : comments) {
            allIds.add(comment.getId());
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                collectAllCommentIds(new ArrayList<>(comment.getReplies()), allIds);
            }
        }
    }

    private void collectAllUserIds(List<Comment> comments, Set<Long> allUserIds) {
        for (Comment comment : comments) {
            allUserIds.add(comment.getUser().getId());
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                collectAllUserIds(new ArrayList<>(comment.getReplies()), allUserIds);
            }
        }
    }

    // Batch loading para comentarios de múltiples posts
    public Map<Long, List<CommentDTO>> getCommentsByMultiplePostIds(List<Long> postIds, Long currentUserId, Map<Long, User> globalBatchUsers) {
        if (postIds.isEmpty()) {
            return new HashMap<>();
        }
        List<Comment> allComments = commentRepository.findCommentsWithRepliesByMultiplePostIds(postIds);
        if (allComments.isEmpty()) {
            return postIds.stream().collect(Collectors.toMap(postId -> postId, postId -> List.of()));
        }

        Map<Long, Comment> allCommentsMap = allComments.stream()
                .collect(Collectors.toMap(Comment::getId, comment -> comment));

        List<Comment> topLevelComments = new ArrayList<>();
        List<Comment> replyComments = new ArrayList<>();

        for (Comment comment : allComments) {
            if (comment.getParentComment() == null) {
                topLevelComments.add(comment);
            } else {
                replyComments.add(comment);
            }
        }

        // Organizar respuestas dentro de sus comentarios padre
        for (Comment reply : replyComments) {
            Comment parent = allCommentsMap.get(reply.getParentComment().getId());
            if (parent != null) {
                parent.getReplies().add(reply);
            }
        }

        Map<Long, List<Comment>> commentsByPost = topLevelComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getPost().getId()));

        Set<Long> allCommentIds = allComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toSet());

        Set<Long> allUserIds = allComments.stream()
                .map(comment -> comment.getUser().getId())
                .collect(Collectors.toSet());

        Map<Long, User> batchUsers = Map.of();
        if (batchUsers == null && !allUserIds.isEmpty()) {
            long batchUsersStart = System.currentTimeMillis();
            batchUsers = userRepository.findAllByIdWithInterests(new ArrayList<>(allUserIds)).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
            long batchUsersEnd = System.currentTimeMillis();
            System.out.println("    👥 Usuarios de comentarios: " + (batchUsersEnd - batchUsersStart) + "ms");
        } else {
            batchUsers = globalBatchUsers;
        }

        Map<Long, Map<String, Long>> batchReactionCounts;
        Map<Long, String> batchUserReactions;

        if (!allCommentIds.isEmpty()) {
            long batchReactionsStart = System.currentTimeMillis();
            batchReactionCounts = reactionService.getReactionsCountForMultipleTargets(
                    new ArrayList<>(allCommentIds), TargetType.COMMENT);
            long batchReactionsEnd = System.currentTimeMillis();
            System.out.println("    👍 Reacciones de comentarios: " + (batchReactionsEnd - batchReactionsStart) + "ms");

            if (currentUserId != null) {
                long batchUserReactionsStart = System.currentTimeMillis();
                batchUserReactions = reactionService.getUserReactionsForMultipleTargets(
                        currentUserId, new ArrayList<>(allCommentIds), TargetType.COMMENT);
                long batchUserReactionsEnd = System.currentTimeMillis();
                System.out.println("    👤 Reacciones usuario comentarios: " + (batchUserReactionsEnd - batchUserReactionsStart) + "ms");
            } else {
                batchUserReactions = null;
            }
        } else {
            batchUserReactions = null;
            batchReactionCounts = null;
        }

        long conversionStart = System.currentTimeMillis();
        Map<Long, List<CommentDTO>> result = new HashMap<>();

        for (Long postId : postIds) {
            List<Comment> postComments = commentsByPost.getOrDefault(postId, List.of());
            Map<Long, User> finalBatchUsers = batchUsers;
            List<CommentDTO> commentDTOs = postComments.stream()
                    .sorted(Comparator.comparing(Comment::getFechaComentario))
                    .map(comment -> {
                        CommentDTO dto = convertToDto(comment, currentUserId, batchReactionCounts, batchUserReactions, finalBatchUsers);

                        // Agregar respuestas si las hay
                        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                            List<CommentDTO> repliesDTO = comment.getReplies().stream()
                                    .sorted(Comparator.comparing(Comment::getFechaComentario))
                                    .map(reply -> convertToDto(reply, currentUserId, batchReactionCounts, batchUserReactions, finalBatchUsers))
                                    .collect(Collectors.toList());
                            dto.setReplies(repliesDTO);
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());
            result.put(postId, commentDTOs);
        }
        return result;
    }

    public Comment createComment(Comment comment, Long userId, Long postId, Long parentCommentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post no encontrado con ID: " + postId));

        comment.setUser(user);
        comment.setPost(post);
        comment.setFechaComentario(OffsetDateTime.now());
        comment.setUltimaActualizacion(OffsetDateTime.now());

        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new EntityNotFoundException("Comentario padre no encontrado con ID: " + parentCommentId));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        CommentDTO commentDTO = new CommentDTO(savedComment);
        if (savedComment.getReplies() != null && !savedComment.getReplies().isEmpty()) {
            commentDTO.setReplies(savedComment.getReplies().stream()
                    .map(reply -> convertToDto(reply, userId, null, null, null))
                    .collect(Collectors.toList()));
        }
        webSocketMessageController.notifyNewComment(commentDTO);
        return savedComment;
    }

    public List<CommentDTO> getCommentsByPostId(Long postId, Long currentUserId) {
        return getCommentsByPostId(postId, currentUserId, null);
    }

    public List<CommentDTO> getCommentsByPostId(Long postId, Long currentUserId, Map<Long, User> batchUsers) {
        List<Comment> topLevelComments = commentRepository.findTopLevelCommentsWithReplies(postId);
        if (topLevelComments.isEmpty()) {
            return List.of();
        }
        Set<Long> allCommentIds = new HashSet<>();
        collectAllCommentIds(topLevelComments, allCommentIds);

        Map<Long, User> commentBatchUsers = batchUsers;
        if (commentBatchUsers == null) {
            Set<Long> allUserIds = new HashSet<>();
            collectAllUserIds(topLevelComments, allUserIds);
            commentBatchUsers = userRepository.findAllByIdWithInterests(new ArrayList<>(allUserIds)).stream()
                    .collect(Collectors.toMap(User::getId, user -> user));
        }

        Map<Long, Map<String, Long>> batchReactionCounts = reactionService.getReactionsCountForMultipleTargets(
                new ArrayList<>(allCommentIds), TargetType.COMMENT);

        Map<Long, String> batchUserReactions;
        if (currentUserId != null) {
            batchUserReactions = reactionService.getUserReactionsForMultipleTargets(
                    currentUserId, new ArrayList<>(allCommentIds), TargetType.COMMENT);
        } else {
            batchUserReactions = null;
        }

        Map<Long, User> finalCommentBatchUsers = commentBatchUsers;
        List<CommentDTO> result = topLevelComments.stream()
                .map(comment -> {
                    CommentDTO dto = convertToDto(comment, currentUserId, batchReactionCounts, batchUserReactions, finalCommentBatchUsers);

                    // Agregar respuestas si las hay
                    if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                        List<CommentDTO> repliesDTO = comment.getReplies().stream()
                                .sorted(Comparator.comparing(Comment::getFechaComentario))
                                .map(reply -> convertToDto(reply, currentUserId, batchReactionCounts, batchUserReactions, finalCommentBatchUsers))
                                .collect(Collectors.toList());
                        dto.setReplies(repliesDTO);
                    }

                    return dto;
                })
                .collect(Collectors.toList());
        return result;
    }

    public List<CommentDTO> getRepliesByParentCommentId(Long parentCommentId, Long currentUserId) {
        List<Comment> replies = commentRepository.findByParentComment_IdOrderByFechaComentarioAsc(parentCommentId);

        if (replies.isEmpty()) {
            return List.of();
        }

        // Para respuestas individuales, usar batch loading también
        Set<Long> allReplyIds = new HashSet<>();
        collectAllCommentIds(replies, allReplyIds);

        Set<Long> allUserIds = new HashSet<>();
        collectAllUserIds(replies, allUserIds);
        Map<Long, User> batchUsers = userRepository.findAllByIdWithInterests(new ArrayList<>(allUserIds)).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        Map<Long, Map<String, Long>> batchReactionCounts = reactionService.getReactionsCountForMultipleTargets(
                new ArrayList<>(allReplyIds), TargetType.COMMENT);
        Map<Long, String> batchUserReactions = currentUserId != null ?
                reactionService.getUserReactionsForMultipleTargets(
                        currentUserId, new ArrayList<>(allReplyIds), TargetType.COMMENT) : null;

        return replies.stream()
                .map(reply -> convertToDto(reply, currentUserId, batchReactionCounts, batchUserReactions, batchUsers))
                .collect(Collectors.toList());
    }

    public Optional<CommentDTO> getCommentById(Long id, Long currentUserId) {
        Optional<Comment> commentOptional = commentRepository.findById(id);
        if (commentOptional.isEmpty()) {
            return Optional.empty();
        }
        // Para un comentario individual, usar métodos individuales (más eficiente)
        Comment comment = commentOptional.get();
        return Optional.of(convertToDto(comment, currentUserId, null, null, null));
    }

    public Comment updateComment(Long id, Comment commentDetails) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comentario no encontrado con ID: " + id));
        existingComment.setContenido(commentDetails.getContenido());
        existingComment.setUltimaActualizacion(OffsetDateTime.now());

        Comment updatedComment = commentRepository.save(existingComment);
        CommentDTO commentDTO = convertToDto(updatedComment, null, null, null, null);
        webSocketMessageController.notifyCommentUpdate(commentDTO);
        return updatedComment;
    }

    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new EntityNotFoundException("Comentario no encontrado con ID: " + id);
        }
        webSocketMessageController.notifyCommentDelete(id);
        commentRepository.deleteById(id);
    }
}