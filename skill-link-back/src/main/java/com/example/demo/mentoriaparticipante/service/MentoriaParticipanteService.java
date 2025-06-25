package com.example.demo.mentoriaparticipante.service;

import com.example.demo.common.ParticipantStatus;
import com.example.demo.mentoria.dto.ParticipantDto;
import com.example.demo.mentoria.model.Mentoria;
import com.example.demo.mentoria.repository.MentoriaRepository;
import com.example.demo.mentoriaparticipante.dto.*;
import com.example.demo.mentoriaparticipante.model.MentoriaParticipante;
import com.example.demo.mentoriaparticipante.repository.MentoriaParticipanteRepository;
import com.example.demo.user.model.User;
import com.example.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MentoriaParticipanteService {

    @Autowired
    private MentoriaParticipanteRepository participanteRepository;

    @Autowired
    private MentoriaRepository mentoriaRepository;

    @Autowired
    private UserRepository userRepository;

    // INSCRIBIRSE A MENTORIA
    @Transactional
    public MentoriaParticipanteRegisterResponse enrollInMentoria(MentoriaParticipanteRegisterRequest request, Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mentoria mentoria = mentoriaRepository.findById(request.mentoriaId())
                .orElseThrow(() -> new RuntimeException("Mentoría no encontrada"));

        // Validaciones
        if (!mentoria.isAvailable()) {
            throw new RuntimeException("La mentoría no está disponible");
        }

        if (!mentoria.hasAvailableSpace()) {
            throw new RuntimeException("La mentoría no tiene espacios disponibles");
        }

        if (mentoria.getMentor().getId().equals(userId)) {
            throw new RuntimeException("No puedes inscribirte en tu propia mentoría");
        }

        if (participanteRepository.existsByMentoriaAndParticipant(mentoria, user)) {
            throw new RuntimeException("Ya estás inscrito en esta mentoría");
        }

        // Crear participación
        MentoriaParticipante participante = new MentoriaParticipante(mentoria, user);
        MentoriaParticipante savedParticipante = participanteRepository.save(participante);

        return convertToRegisterResponse(savedParticipante);
    }

    // OBTENER PARTICIPACIÓN POR ID
    public Optional<MentoriaParticipanteDetailResponse> getParticipacionById(Long id) {
        return participanteRepository.findById(id)
                .map(this::convertToDetailResponse);
    }

    // OBTENER PARTICIPACIONES DE UN USUARIO
    public List<MentoriaParticipanteListResponse> getParticipacionesByUser(Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return participanteRepository.findByParticipant(user)
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // OBTENER PARTICIPACIONES ACTIVAS DE UN USUARIO
    public List<MentoriaParticipanteListResponse> getActiveParticipacionesByUser(Long userId) {
        return participanteRepository.findActiveParticipationsByUserId(userId)
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // OBTENER PARTICIPANTES DE UNA MENTORIA
    public List<MentoriaParticipanteDetailResponse> getParticipantesByMentoria(Long mentoriaId, Long mentorId) {
        Mentoria mentoria = mentoriaRepository.findById(mentoriaId)
                .orElseThrow(() -> new RuntimeException("Mentoría no encontrada"));

        // Verificar que el usuario es el mentor
        if (!mentoria.getMentor().getId().equals(mentorId)) {
            throw new RuntimeException("No tienes permisos para ver los participantes de esta mentoría");
        }

        return participanteRepository.findByMentoria(mentoria)
                .stream()
                .map(this::convertToDetailResponse)
                .toList();
    }

    // ACTUALIZAR ESTADO DE PARTICIPACIÓN
    @Transactional
    public MentoriaParticipanteUpdateResponse updateParticipacionStatus(
            Long participacionId,
            MentoriaParticipanteUpdateRequest request,
            Long userId) {

        MentoriaParticipante participacion = participanteRepository.findById(participacionId)
                .orElseThrow(() -> new RuntimeException("Participación no encontrada"));

        // Verificar permisos: solo el mentor o el participante pueden actualizar
        boolean isMentor = participacion.getMentoria().getMentor().getId().equals(userId);
        boolean isParticipant = participacion.getParticipant().getId().equals(userId);

        if (!isMentor && !isParticipant) {
            throw new RuntimeException("No tienes permisos para actualizar esta participación");
        }

        // Validar transiciones de estado
        if (request.status() != null) {
            validateStatusTransition(participacion.getStatus(), request.status(), isMentor);
            participacion.setStatus(request.status());
        }

        MentoriaParticipante updatedParticipacion = participanteRepository.save(participacion);

        String message = switch (request.status()) {
            case COMPLETADO -> "Participación marcada como completada";
            case ABANDONADO -> "Participación marcada como abandonada";
            case INSCRITO -> "Participación reactivada";
        };

        return new MentoriaParticipanteUpdateResponse(
                updatedParticipacion.getId(),
                updatedParticipacion.getRegistrationDate(),
                updatedParticipacion.getStatus(),
                message
        );
    }

    // ABANDONAR MENTORIA
    @Transactional
    public void leaveMentoria(Long mentoriaId, Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mentoria mentoria = mentoriaRepository.findById(mentoriaId)
                .orElseThrow(() -> new RuntimeException("Mentoría no encontrada"));

        MentoriaParticipante participacion = participanteRepository.findByMentoriaAndParticipant(mentoria, user)
                .orElseThrow(() -> new RuntimeException("No estás inscrito en esta mentoría"));

        if (participacion.getStatus() != ParticipantStatus.INSCRITO) {
            throw new RuntimeException("No puedes abandonar una mentoría que no está activa");
        }

        participacion.markAsAbandoned();
        participanteRepository.save(participacion);
    }

    // COMPLETAR MENTORIA (solo el mentor puede marcar como completado)
    @Transactional
    public void completeMentoria(Long mentoriaId, Long participantId, Long mentorId) {
        Mentoria mentoria = mentoriaRepository.findById(mentoriaId)
                .orElseThrow(() -> new RuntimeException("Mentoría no encontrada"));

        // Verificar que el usuario es el mentor
        if (!mentoria.getMentor().getId().equals(mentorId)) {
            throw new RuntimeException("Solo el mentor puede marcar participaciones como completadas");
        }

        User participant = userRepository.findByIdAndActiveTrue(participantId)
                .orElseThrow(() -> new RuntimeException("Participante no encontrado"));

        MentoriaParticipante participacion = participanteRepository.findByMentoriaAndParticipant(mentoria, participant)
                .orElseThrow(() -> new RuntimeException("Participación no encontrada"));

        if (participacion.getStatus() != ParticipantStatus.INSCRITO) {
            throw new RuntimeException("Solo se pueden completar participaciones activas");
        }

        participacion.markAsCompleted();
        participanteRepository.save(participacion);
    }

    // VERIFICAR SI USUARIO ESTÁ INSCRITO EN MENTORIA
    public boolean isUserEnrolled(Long mentoriaId, Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mentoria mentoria = mentoriaRepository.findById(mentoriaId)
                .orElseThrow(() -> new RuntimeException("Mentoría no encontrada"));

        return participanteRepository.existsByMentoriaAndParticipant(mentoria, user);
    }

    // OBTENER ESTADÍSTICAS DE UN MENTOR
    public Map<String, Object> getMentorStats(Long mentorId) {
        List<MentoriaParticipante> allParticipations = participanteRepository.findParticipantsByMentorId(mentorId);

        long totalParticipants = allParticipations.size();
        long activeParticipants = allParticipations.stream()
                .filter(p -> p.getStatus() == ParticipantStatus.INSCRITO)
                .count();
        long completedParticipants = allParticipations.stream()
                .filter(p -> p.getStatus() == ParticipantStatus.COMPLETADO)
                .count();
        long abandonedParticipants = allParticipations.stream()
                .filter(p -> p.getStatus() == ParticipantStatus.ABANDONADO)
                .count();

        return Map.of(
                "totalParticipants", totalParticipants,
                "activeParticipants", activeParticipants,
                "completedParticipants", completedParticipants,
                "abandonedParticipants", abandonedParticipants,
                "completionRate", totalParticipants > 0 ? (completedParticipants * 100.0 / totalParticipants) : 0
        );
    }

    // Validar transiciones de estado
    private void validateStatusTransition(
            ParticipantStatus currentStatus,
            ParticipantStatus newStatus,
            boolean isMentor) {

        // Solo el mentor puede marcar como completado
        if (newStatus == ParticipantStatus.COMPLETADO && !isMentor) {
            throw new RuntimeException("Solo el mentor puede marcar participaciones como completadas");
        }

        // No se puede cambiar desde completado
        if (currentStatus == ParticipantStatus.COMPLETADO) {
            throw new RuntimeException("No se puede cambiar el estado de una participación completada");
        }
    }

    // Métodos de conversión
    private MentoriaParticipanteRegisterResponse convertToRegisterResponse(MentoriaParticipante participante) {
        return new MentoriaParticipanteRegisterResponse(
                participante.getId(),
                new MentoriaDto(
                        participante.getMentoria().getId(),
                        participante.getMentoria().getTitle(),
                        participante.getMentoria().getDescription(),
                        participante.getMentoria().getMentor().getName() + " " +
                                participante.getMentoria().getMentor().getSecondName(),
                        participante.getMentoria().getMaxPersons(),
                        participante.getMentoria().getCurrentParticipants()
                ),
                new ParticipantDto(
                        participante.getParticipant().getId(),
                        participante.getParticipant().getName(),
                        participante.getParticipant().getSecondName(),
                        participante.getParticipant().getEmail(),
                        participante.getRegistrationDate(),
                        participante.getStatus().toString()
                ),
                participante.getRegistrationDate(),
                participante.getStatus(),
                "Te has inscrito exitosamente en la mentoría"
        );
    }

    private MentoriaParticipanteListResponse convertToListResponse(MentoriaParticipante participante) {
        return new MentoriaParticipanteListResponse(
                participante.getId(),
                participante.getMentoria().getTitle(),
                participante.getMentoria().getMentor().getName() + " " +
                        participante.getMentoria().getMentor().getSecondName(),
                participante.getRegistrationDate(),
                participante.getStatus(),
                participante.getParticipant().isActive()
        );
    }

    private MentoriaParticipanteDetailResponse convertToDetailResponse(MentoriaParticipante participante) {
        return new MentoriaParticipanteDetailResponse(
                participante.getId(),
                new MentoriaDto(
                        participante.getMentoria().getId(),
                        participante.getMentoria().getTitle(),
                        participante.getMentoria().getDescription(),
                        participante.getMentoria().getMentor().getName() + " " +
                                participante.getMentoria().getMentor().getSecondName(),
                        participante.getMentoria().getMaxPersons(),
                        participante.getMentoria().getCurrentParticipants()
                ),
                new ParticipantDto(
                        participante.getId(),
                        participante.getParticipant().getName(),
                        participante.getParticipant().getSecondName(),
                        participante.getParticipant().getEmail(),
                        participante.getParticipant().getRegistrationDate(),
                        participante.getStatus().toString()
                ),
                participante.getRegistrationDate(),
                participante.getStatus(),
                participante.getParticipant().isActive()
        );
    }
}