package com.example.demo.mentoriaparticipante.repository;

import com.example.demo.common.ParticipantStatus;
import com.example.demo.mentoria.model.Mentoria;
import com.example.demo.mentoriaparticipante.model.MentoriaParticipante;
import com.example.demo.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentoriaParticipanteRepository extends JpaRepository<MentoriaParticipante, Long> {

    // Verificar si un usuario ya está inscrito en una mentoría
    boolean existsByMentoriaAndParticipant(Mentoria mentoria, User participant);

    // Buscar participación específica
    Optional<MentoriaParticipante> findByMentoriaAndParticipant(Mentoria mentoria, User participant);

    // Buscar todas las participaciones de una mentoría
    List<MentoriaParticipante> findByMentoria(Mentoria mentoria);

    // Buscar participaciones activas de una mentoría
    List<MentoriaParticipante> findByMentoriaAndStatus(Mentoria mentoria, ParticipantStatus status);

    // Buscar todas las participaciones de un usuario
    List<MentoriaParticipante> findByParticipant(User participant);

    // Buscar participaciones de un usuario por estado
    List<MentoriaParticipante> findByParticipantAndStatus(User participant, ParticipantStatus status);

    // Buscar participaciones activas de un usuario
    @Query("SELECT mp FROM MentoriaParticipante mp WHERE mp.participant.id = :userId AND mp.status = 'INSCRITO'")
    List<MentoriaParticipante> findActiveParticipationsByUserId(@Param("userId") Long userId);

    // Contar participantes activos de una mentoría
    @Query("SELECT COUNT(mp) FROM MentoriaParticipante mp WHERE mp.mentoria.id = :mentoriaId AND mp.status = 'INSCRITO'")
    long countActiveParticipantsByMentoriaId(@Param("mentoriaId") Long mentoriaId);

    // Contar participaciones completadas de un usuario
    @Query("SELECT COUNT(mp) FROM MentoriaParticipante mp WHERE mp.participant.id = :userId AND mp.status = 'COMPLETADO'")
    long countCompletedParticipationsByUserId(@Param("userId") Long userId);

    // Buscar participaciones por mentoría y estado
    List<MentoriaParticipante> findByMentoriaIdAndStatus(Long mentoriaId, ParticipantStatus status);

    // Buscar participaciones por usuario y estado
    List<MentoriaParticipante> findByParticipantIdAndStatus(Long participantId, ParticipantStatus status);

    // Eliminar todas las participaciones de una mentoría
    void deleteByMentoria(Mentoria mentoria);

    // Eliminar todas las participaciones de un usuario
    void deleteByParticipant(User participant);

    // Buscar mentorías donde un usuario es mentor y tiene participantes
    @Query("SELECT mp FROM MentoriaParticipante mp WHERE mp.mentoria.mentor.id = :mentorId")
    List<MentoriaParticipante> findParticipantsByMentorId(@Param("mentorId") Long mentorId);

    // Verificar si un usuario puede inscribirse (no está ya inscrito y hay espacio)
    @Query("SELECT CASE WHEN COUNT(mp) > 0 THEN false ELSE true END FROM MentoriaParticipante mp " +
            "WHERE mp.mentoria.id = :mentoriaId AND mp.participant.id = :userId AND mp.status IN ('INSCRITO', 'COMPLETADO')")
    boolean canUserEnroll(@Param("mentoriaId") Long mentoriaId, @Param("userId") Long userId);
}