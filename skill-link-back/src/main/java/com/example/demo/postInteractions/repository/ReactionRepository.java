package com.example.demo.postInteractions.repository;

import com.example.demo.postInteractions.model.Reaction;
import com.example.demo.postInteractions.model.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Reaction.ReactionId> {

    Optional<Reaction> findById_UserIdAndId_TargetIdAndId_TargetType(Long userId, Long targetId, TargetType targetType);
    void deleteById_UserIdAndId_TargetIdAndId_TargetType(Long userId, Long targetId, TargetType targetType);

    // Consulta JPQL explícita para contar reacciones por tipo para un target específico
    @Query("SELECT rt.nombreReaccion, COUNT(r) " +
            "FROM Reaction r JOIN r.reactionType rt " +
            "WHERE r.id.targetId = :targetId AND r.id.targetType = :targetType " +
            "GROUP BY rt.nombreReaccion")
    List<Object[]> countReactionsByTargetIdAndTargetType(@Param("targetId") Long targetId, @Param("targetType") TargetType targetType);

    @Query("SELECT r.id.targetId, rt.nombreReaccion, COUNT(r) " +
            "FROM Reaction r JOIN r.reactionType rt " +
            "WHERE r.id.targetId IN :targetIds AND r.id.targetType = :targetType " +
            "GROUP BY r.id.targetId, rt.nombreReaccion")
    List<Object[]> countReactionsByMultipleTargets(@Param("targetIds") List<Long> targetIds, @Param("targetType") TargetType targetType);

    // Consulta JPQL explícita para obtener el tipo de reacción de un usuario para un target
    @Query("SELECT r.reactionType.nombreReaccion FROM Reaction r " +
            "WHERE r.id.userId = :userId AND r.id.targetId = :targetId AND r.id.targetType = :targetType")
    Optional<String> findUserReactionTypeByUserIdAndTargetIdAndTargetType(
            @Param("userId") Long userId,
            @Param("targetId") Long targetId,
            @Param("targetType") TargetType targetType);

    // Consulta batch para reacciones de usuario en múltiples targets
    @Query("SELECT r.id.targetId, r.reactionType.nombreReaccion " +
            "FROM Reaction r " +
            "WHERE r.id.userId = :userId AND r.id.targetId IN :targetIds AND r.id.targetType = :targetType")
    List<Object[]> findUserReactionsForMultipleTargets(
            @Param("userId") Long userId,
            @Param("targetIds") List<Long> targetIds,
            @Param("targetType") TargetType targetType);
}