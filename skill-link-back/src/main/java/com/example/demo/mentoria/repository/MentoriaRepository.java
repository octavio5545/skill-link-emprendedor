package com.example.demo.mentoria.repository;

import com.example.demo.common.MentoriaStatus;
import com.example.demo.common.UserInterest;
import com.example.demo.mentoria.model.Mentoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentoriaRepository extends JpaRepository<Mentoria, Long> {

    // Buscar por ID (sin filtro de activo porque no tiene campo activo)
    Optional<Mentoria> findById(Long id);

    // Buscar por mentor
    List<Mentoria> findByMentorId(Long mentorId);
    Page<Mentoria> findByMentorId(Long mentorId, Pageable pageable);

    // Buscar por estado
    List<Mentoria> findByStatus(MentoriaStatus status);
    Page<Mentoria> findByStatus(MentoriaStatus status, Pageable pageable);

    // Buscar mentorias disponibles
    @Query("SELECT m FROM Mentoria m WHERE m.status = 'DISPONIBLE'")
    List<Mentoria> findAvailableMentorias();

    // Buscar mentorias con espacio disponible
    @Query("SELECT m FROM Mentoria m WHERE m.status = 'DISPONIBLE' AND (m.maxPersons IS NULL OR SIZE(m.participants) < m.maxPersons)")
    List<Mentoria> findMentoriasWithAvailableSpace();

    // Buscar por título (case insensitive)
    List<Mentoria> findByTitleContainingIgnoreCase(String title);

    // Buscar por interés específico
    @Query("SELECT m FROM Mentoria m JOIN m.interests i WHERE i = :interest ORDER BY m.creationDate DESC")
    List<Mentoria> findByInterestsContaining(@Param("interest") UserInterest interest);

    // Buscar por múltiples intereses
    @Query("SELECT DISTINCT m FROM Mentoria m JOIN m.interests i WHERE i IN :interests ORDER BY m.creationDate DESC")
    List<Mentoria> findByInterestsIn(@Param("interests") List<UserInterest> interests);

    // Mentorias más populares (por número de participantes)
    @Query("SELECT m FROM Mentoria m ORDER BY SIZE(m.participants) DESC")
    List<Mentoria> findMostPopularMentorias(Pageable pageable);

    // Mentorias recientes
    @Query("SELECT m FROM Mentoria m ORDER BY m.creationDate DESC")
    List<Mentoria> findRecentMentorias(Pageable pageable);

    // Contar mentorias por mentor
    long countByMentorId(Long mentorId);

    // Contar mentorias por estado
    long countByStatus(MentoriaStatus status);

    // Contar mentorias por interés
    @Query("SELECT COUNT(DISTINCT m) FROM Mentoria m JOIN m.interests i WHERE i = :interest")
    long countByInterest(@Param("interest") UserInterest interest);

    // Buscar mentorias donde un usuario es participante
    @Query("SELECT m FROM Mentoria m JOIN m.participants p WHERE p.participant.id = :userId")
    List<Mentoria> findMentoriasByParticipantId(@Param("userId") Long userId);

    // Buscar mentorias disponibles por intereses del usuario
    @Query("SELECT DISTINCT m FROM Mentoria m JOIN m.interests i WHERE i IN :interests AND m.status = 'DISPONIBLE'")
    List<Mentoria> findAvailableMentoriasByInterests(@Param("interests") List<UserInterest> interests);
}
