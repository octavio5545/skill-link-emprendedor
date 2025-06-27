package com.example.demo.postInteractions.service;

import com.example.demo.postInteractions.model.TargetType;
import com.example.demo.postInteractions.controller.WebSocketMessageController;
import com.example.demo.postInteractions.model.Reaction;
import com.example.demo.postInteractions.model.ReactionType;
import com.example.demo.user.model.User;
import com.example.demo.postInteractions.repository.*;
import com.example.demo.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final ReactionTypeRepository reactionTypeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final WebSocketMessageController webSocketMessageController;

    private static Map<String, Long> reactionTypesCache = null;

    @Autowired
    public ReactionService(ReactionRepository reactionRepository,
                           ReactionTypeRepository reactionTypeRepository,
                           UserRepository userRepository,
                           PostRepository postRepository,
                           CommentRepository commentRepository,
                           WebSocketMessageController webSocketMessageController) {
        this.reactionRepository = reactionRepository;
        this.reactionTypeRepository = reactionTypeRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.webSocketMessageController = webSocketMessageController;
    }

    private Map<String, Long> getReactionTypesCache() {
        if (reactionTypesCache == null) {
            synchronized (ReactionService.class) {
                if (reactionTypesCache == null) {
                    long startTime = System.currentTimeMillis();
                    reactionTypesCache = reactionTypeRepository.findAll().stream()
                            .collect(Collectors.toMap(
                                    ReactionType::getNombreReaccion,
                                    rt -> 0L
                            ));
                    long totalTime = System.currentTimeMillis() - startTime;
                }
            }
        }
        return new HashMap<>(reactionTypesCache); // Retornar copia para evitar modificaciones
    }

    public Map<Long, Map<String, Long>> getReactionsCountForMultipleTargets(List<Long> targetIds, TargetType targetType) {
        long startTime = System.currentTimeMillis();

        if (targetIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Object[]> batchCounts = reactionRepository.countReactionsByMultipleTargets(targetIds, targetType);

        Map<Long, Map<String, Long>> result = new HashMap<>();
        for (Long targetId : targetIds) {
            result.put(targetId, getReactionTypesCache());
        }
        for (Object[] count : batchCounts) {
            Long targetId = (Long) count[0];
            String reactionTypeName = (String) count[1];
            Long countValue = (Long) count[2];

            result.get(targetId).put(reactionTypeName, countValue);
        }

        return result;
    }

    public Map<Long, String> getUserReactionsForMultipleTargets(Long userId, List<Long> targetIds, TargetType targetType) {
        long startTime = System.currentTimeMillis();

        if (targetIds.isEmpty() || userId == null) {
            return new HashMap<>();
        }

        List<Object[]> userReactions = reactionRepository.findUserReactionsForMultipleTargets(userId, targetIds, targetType);

        Map<Long, String> result = userReactions.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0], // targetId
                        row -> (String) row[1] // reactionTypeName
                ));

        long totalTime = System.currentTimeMillis() - startTime;

        return result;
    }

    public Reaction createOrUpdateReaction(Long userId, Long targetId, TargetType targetType, Long reactionTypeId) {
        System.out.println("=== DEBUG REACTION SERVICE ===");
        System.out.println("userId: " + userId);
        System.out.println("targetId: " + targetId);
        System.out.println("targetType: " + targetType);
        System.out.println("reactionTypeId: " + reactionTypeId);

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        if (targetType == TargetType.POST && !postRepository.existsById(targetId)) {
            throw new EntityNotFoundException("Post no encontrado con ID: " + targetId);
        } else if (targetType == TargetType.COMMENT && !commentRepository.existsById(targetId)) {
            throw new EntityNotFoundException("Comentario no encontrado con ID: " + targetId);
        }

        ReactionType reactionType = reactionTypeRepository.findById(reactionTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de reacción no encontrado con ID: " + reactionTypeId));

        System.out.println("ReactionType encontrado: " + reactionType.getNombreReaccion());

        Optional<Reaction> existingReactionOpt = reactionRepository.findById_UserIdAndId_TargetIdAndId_TargetType(userId, targetId, targetType);

        Reaction savedReaction = null;

        if (existingReactionOpt.isPresent()) {
            Reaction existingReaction = existingReactionOpt.get();
            Long existingReactionTypeId = existingReaction.getReactionType().getId();

            if (existingReactionTypeId.equals(reactionTypeId)) {
                System.out.println("ELIMINANDO reacción (toggle off)");
                reactionRepository.delete(existingReaction);
                savedReaction = null; // Indica eliminación
            } else {
                // Si el tipo de reacción es diferente, actualizamos la reacción existente
                System.out.println("ACTUALIZANDO reacción existente");
                existingReaction.setReactionType(reactionType);
                existingReaction.setFechaReaccion(LocalDateTime.now().atOffset(ZoneOffset.UTC));
                savedReaction = reactionRepository.save(existingReaction);
                System.out.println("Reacción actualizada exitosamente");
            }
        } else {
            Reaction newReaction = new Reaction();
            newReaction.setId(new Reaction.ReactionId(userId, targetId, targetType));

            User userRef = userRepository.getReferenceById(userId);
            newReaction.setUser(userRef);
            newReaction.setReactionType(reactionType);
            newReaction.setFechaReaccion(LocalDateTime.now().atOffset(ZoneOffset.UTC));
            savedReaction = reactionRepository.save(newReaction);
            System.out.println("Nueva reacción creada exitosamente");
        }

        long countStartTime = System.currentTimeMillis();
        Map<String, Long> updatedCounts = getReactionsCountForTargetOptimized(targetId, targetType);
        long countEndTime = System.currentTimeMillis();
        System.out.println("Conteos actualizados en: " + (countEndTime - countStartTime) + "ms - " + updatedCounts);

        webSocketMessageController.notifyReactionChange(targetId, targetType, updatedCounts);

        System.out.println("=== FIN DEBUG REACTION SERVICE ===");
        return savedReaction;
    }

    private Map<String, Long> getReactionsCountForTargetOptimized(Long targetId, TargetType targetType) {
        List<Object[]> counts = reactionRepository.countReactionsByTargetIdAndTargetType(targetId, targetType);
        Map<String, Long> reactionCounts = getReactionTypesCache();

        for (Object[] count : counts) {
            String reactionTypeName = (String) count[0];
            Long countValue = (Long) count[1];
            reactionCounts.put(reactionTypeName, countValue);
        }

        return reactionCounts;
    }

    public void deleteReaction(Long userId, Long targetId, TargetType targetType) {
        Optional<Reaction> existingReactionOpt = reactionRepository.findById_UserIdAndId_TargetIdAndId_TargetType(userId, targetId, targetType);
        if (existingReactionOpt.isEmpty()) {
            throw new EntityNotFoundException("Reacción no encontrada para eliminar.");
        }
        reactionRepository.deleteById_UserIdAndId_TargetIdAndId_TargetType(userId, targetId, targetType);

        Map<String, Long> updatedCounts = getReactionsCountForTargetOptimized(targetId, targetType);
        webSocketMessageController.notifyReactionChange(targetId, targetType, updatedCounts);
    }

    public Map<String, Long> getReactionsCountForTarget(Long targetId, TargetType targetType) {
        return getReactionsCountForTargetOptimized(targetId, targetType);
    }

    public String getUserReactionForTarget(Long userId, Long targetId, TargetType targetType) {
        Optional<String> reactionName = reactionRepository.findUserReactionTypeByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);
        return reactionName.orElse(null);
    }
}