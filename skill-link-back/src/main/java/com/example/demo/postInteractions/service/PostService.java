package com.example.demo.postInteractions.service;

import com.example.demo.postInteractions.controller.WebSocketMessageController;
import com.example.demo.postInteractions.dto.CommentDTO;
import com.example.demo.postInteractions.dto.PostDTO;
import com.example.demo.postInteractions.model.Post;
import com.example.demo.postInteractions.model.TargetType;
import com.example.demo.user.model.User;
import com.example.demo.postInteractions.repository.PostRepository;
import com.example.demo.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReactionService reactionService;
    private final CommentService commentService;
    private final WebSocketMessageController webSocketMessageController;

    @Autowired
    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       ReactionService reactionService,
                       CommentService commentService,
                       WebSocketMessageController webSocketMessageController) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.reactionService = reactionService;
        this.commentService = commentService;
        this.webSocketMessageController = webSocketMessageController;
    }

    private PostDTO convertToDto(Post post, Long currentUserId,
                                 Map<Long, Map<String, Long>> batchReactionCounts,
                                 Map<Long, String> batchUserReactions,
                                 Map<Long, User> batchUsers) {
        long startTime = System.currentTimeMillis();

        if (post == null) {
            return null;
        }

        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId().toString());
        postDTO.setTitle(post.getTitulo());
        postDTO.setContent(post.getContenido());
        postDTO.setCreatedAt(post.getFechaPublicacion());

        if (batchUsers != null && batchUsers.containsKey(post.getUser().getId())) {
            User user = batchUsers.get(post.getUser().getId());
            postDTO.setAuthor(new com.example.demo.postInteractions.dto.UserDTO(user));
        } else {
            // Fallback individual solo si no hay batch
            postDTO.setAuthor(new com.example.demo.postInteractions.dto.UserDTO(post.getUser()));
        }

        if (post.getTags() != null && !post.getTags().isEmpty()) {
            postDTO.setTags(post.getTags().stream()
                    .map(tag -> tag.getNombreEtiqueta())
                    .collect(Collectors.toList()));
        } else {
            postDTO.setTags(List.of());
        }

        long basicDtoTime = System.currentTimeMillis();
        System.out.println("📝 [POST-" + post.getId() + "] DTO básico creado en: " + (basicDtoTime - startTime) + "ms");

        long reactionStartTime = System.currentTimeMillis();

        if (batchReactionCounts != null && batchReactionCounts.containsKey(post.getId())) {
            Map<String, Long> reactionsLong = batchReactionCounts.get(post.getId());
            Map<String, Integer> reactionsWithCounts = reactionsLong.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));

            if (!reactionsWithCounts.isEmpty()) {
                postDTO.setReactions(reactionsWithCounts);
            }
        } else {
            Map<String, Long> reactionsLong = reactionService.getReactionsCountForTarget(post.getId(), TargetType.POST);
            Map<String, Integer> reactionsWithCounts = reactionsLong.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));

            if (!reactionsWithCounts.isEmpty()) {
                postDTO.setReactions(reactionsWithCounts);
            }
        }

        if (currentUserId != null) {
            long userReactionStartTime = System.currentTimeMillis();
            if (batchUserReactions != null) {
                String userReaction = batchUserReactions.get(post.getId());
                if (userReaction != null) {
                    postDTO.setUserReaction(userReaction);
                }
            } else {
                String userReaction = reactionService.getUserReactionForTarget(currentUserId, post.getId(), TargetType.POST);
                if (userReaction != null) {
                    postDTO.setUserReaction(userReaction);
                }
            }
        }

        long commentsStartTime = System.currentTimeMillis();
        List<CommentDTO> commentDTOs = commentService.getCommentsByPostId(post.getId(), currentUserId, batchUsers);
        if (!commentDTOs.isEmpty()) {
            postDTO.setComments(commentDTOs);
        }
        return postDTO;
    }

    public List<PostDTO> getAllPosts(Long currentUserId) {
        List<Post> posts = postRepository.findAll();
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        List<Post> postsWithTags = postRepository.findAllByIdWithTags(postIds);
        Map<Long, Post> postMap = postsWithTags.stream()
                .collect(Collectors.toMap(Post::getId, post -> post));
        long tagsEndTime = System.currentTimeMillis();

        List<Long> userIds = posts.stream().map(post -> post.getUser().getId()).distinct().collect(Collectors.toList());
        Map<Long, User> batchUsers = userRepository.findAllByIdWithInterests(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        long batchUsersEndTime = System.currentTimeMillis();

        Map<Long, Map<String, Long>> batchReactionCounts = reactionService.getReactionsCountForMultipleTargets(postIds, TargetType.POST);

        Map<Long, String> batchUserReactions;
        if (currentUserId != null) {
            long batchUserReactionsStartTime = System.currentTimeMillis();
            batchUserReactions = reactionService.getUserReactionsForMultipleTargets(currentUserId, postIds, TargetType.POST);
            long batchUserReactionsEndTime = System.currentTimeMillis();
        } else {
            batchUserReactions = null;
        }

        long conversionStartTime = System.currentTimeMillis();
        List<PostDTO> postDTOs = posts.stream()
                .map(post -> {
                    // Usar el post con tags cargados
                    Post postWithTags = postMap.get(post.getId());
                    return convertToDto(postWithTags != null ? postWithTags : post,
                            currentUserId, batchReactionCounts, batchUserReactions, batchUsers);
                })
                .collect(Collectors.toList());
        return postDTOs;
    }

    public Optional<PostDTO> getPostById(Long id, Long currentUserId) {
        Optional<Post> postOptional = postRepository.findById(id);
        if (postOptional.isEmpty()) {
            return Optional.empty();
        }

        Post post = postOptional.get();

        // Para un solo post, usar métodos individuales (más eficiente)
        Optional<PostDTO> result = Optional.of(convertToDto(post, currentUserId, null, null, null));

        return result;
    }

    public Post createPost(Post post, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));

        post.setUser(user);
        post.setFechaPublicacion(OffsetDateTime.now());
        post.setUltimaActualizacion(OffsetDateTime.now());

        return postRepository.save(post);
    }

    public Post updatePost(Long id, Post postDetails) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post no encontrado con ID: " + id));

        existingPost.setTitulo(postDetails.getTitulo());
        existingPost.setContenido(postDetails.getContenido());
        existingPost.setUltimaActualizacion(OffsetDateTime.now());

        Post updatedPost = postRepository.save(existingPost);

        PostDTO postDTO = convertToDto(updatedPost, null, null, null, null);
        webSocketMessageController.notifyPostUpdate(postDTO);

        return updatedPost;
    }

    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("Post no encontrado con ID: " + id);
        }

        webSocketMessageController.notifyPostDelete(id);

        postRepository.deleteById(id);
    }

    public List<PostDTO> getPostsByUserId(Long userId, Long currentUserId) {
        List<Post> posts = postRepository.findByUser_Id(userId);

        if (posts.isEmpty()) {
            return List.of();
        }

        // Para posts de usuario, usar batch loading también
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        // Batch usuarios
        List<Long> userIds = posts.stream().map(post -> post.getUser().getId()).distinct().collect(Collectors.toList());
        Map<Long, User> batchUsers = userRepository.findAllByIdWithInterests(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Map<Long, Map<String, Long>> batchReactionCounts = reactionService.getReactionsCountForMultipleTargets(postIds, TargetType.POST);
        Map<Long, String> batchUserReactions = currentUserId != null ?
                reactionService.getUserReactionsForMultipleTargets(currentUserId, postIds, TargetType.POST) : null;

        List<PostDTO> result = posts.stream()
                .map(post -> convertToDto(post, currentUserId, batchReactionCounts, batchUserReactions, batchUsers))
                .collect(Collectors.toList());
        return result;
    }
}