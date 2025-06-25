package com.example.demo.mentoria.service;

import com.example.demo.common.MentoriaStatus;
import com.example.demo.common.ParticipantStatus;
import com.example.demo.common.UserInterest;
import com.example.demo.mentoria.dto.*;
import com.example.demo.mentoria.model.Mentoria;
import com.example.demo.mentoria.repository.MentoriaRepository;
import com.example.demo.user.model.User;
import com.example.demo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MentoriaService {

    @Autowired
    private MentoriaRepository mentoriaRepository;

    @Autowired
    private UserRepository userRepository;

    // CREAR MENTORIA
    @Transactional
    public MentoriaRegisterResponse createMentoria(MentoriaRegisterRequest request, Long mentorId) {
        User mentor = userRepository.findByIdAndActiveTrue(mentorId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Mentoria mentoria = new Mentoria(
                request.title(),
                request.description(),
                request.maxPersons(),
                mentor,
                request.interests()
        );

        Mentoria savedMentoria = mentoriaRepository.save(mentoria);
        return convertToRegisterResponse(savedMentoria);
    }

    // OBTENER TODAS LAS MENTORIAS (con paginación)
    public Page<MentoriaListResponse> getAllMentorias(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("creationDate").descending());
        return mentoriaRepository.findAll(pageable)
                .map(this::convertToListResponse);
    }

    // OBTENER MENTORIA POR ID
    public Optional<MentoriaDetailResponse> getMentoriaById(Long id) {
        return mentoriaRepository.findById(id)
                .map(this::convertToDetailResponse);
    }

    // OBTENER MENTORIAS POR MENTOR
    public List<MentoriaListResponse> getMentoriasByMentor(Long mentorId) {
        return mentoriaRepository.findByMentorId(mentorId)
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // OBTENER MENTORIAS DISPONIBLES
    public List<MentoriaListResponse> getAvailableMentorias() {
        return mentoriaRepository.findAvailableMentorias()
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // OBTENER MENTORIAS CON ESPACIO DISPONIBLE
    public List<MentoriaListResponse> getMentoriasWithAvailableSpace() {
        return mentoriaRepository.findMentoriasWithAvailableSpace()
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // OBTENER MENTORIAS POR INTERÉS
    public List<MentoriaListResponse> getMentoriasByInterest(UserInterest interest) {
        return mentoriaRepository.findByInterestsContaining(interest)
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // BUSCAR MENTORIAS POR TÍTULO
    public List<MentoriaListResponse> searchMentoriasByTitle(String title) {
        return mentoriaRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // OBTENER MENTORIAS POPULARES
    public List<MentoriaListResponse> getMostPopularMentorias(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return mentoriaRepository.findMostPopularMentorias(pageable)
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // OBTENER MENTORIAS RECIENTES
    public List<MentoriaListResponse> getRecentMentorias(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return mentoriaRepository.findRecentMentorias(pageable)
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // ACTUALIZAR MENTORIA
    @Transactional
    public MentoriaUpdateResponse updateMentoria(Long mentoriaId, MentoriaUpdateRequest request, Long userId) {
        Mentoria mentoria = mentoriaRepository.findById(mentoriaId)
                .orElseThrow(() -> new RuntimeException("Mentoría no encontrada"));

        // Verificar que el usuario es el mentor
        if (!mentoria.getMentor().getId().equals(userId)) {
            throw new RuntimeException("No tienes permisos para editar esta mentoría");
        }

        if (request.title() != null && !request.title().trim().isEmpty()) {
            mentoria.setTitle(request.title());
        }
        if (request.description() != null && !request.description().trim().isEmpty()) {
            mentoria.setDescription(request.description());
        }
        if (request.maxPersons() != null) {
            mentoria.setMaxPeople(request.maxPersons());
        }
        if (request.interests() != null && !request.interests().isEmpty()) {
            mentoria.setInterests(request.interests());
        }
        if (request.status() != null) {
            mentoria.setStatus(request.status());
        }

        Mentoria updatedMentoria = mentoriaRepository.save(mentoria);
        return convertToUpdateResponse(updatedMentoria);
    }

    // CAMBIAR ESTADO DE MENTORIA
    @Transactional
    public void changeMentoriaStatus(Long mentoriaId, MentoriaStatus status, Long userId) {
        Mentoria mentoria = mentoriaRepository.findById(mentoriaId)
                .orElseThrow(() -> new RuntimeException("Mentoría no encontrada"));

        // Verificar que el usuario es el mentor
        if (!mentoria.getMentor().getId().equals(userId)) {
            throw new RuntimeException("No tienes permisos para cambiar el estado de esta mentoría");
        }

        mentoria.setStatus(status);
        mentoriaRepository.save(mentoria);
    }

    // ELIMINAR MENTORIA (solo si no tiene participantes)
    @Transactional
    public void deleteMentoria(Long mentoriaId, Long userId) {
        Mentoria mentoria = mentoriaRepository.findById(mentoriaId)
                .orElseThrow(() -> new RuntimeException("Mentoría no encontrada"));

        // Verificar que el usuario es el mentor
        if (!mentoria.getMentor().getId().equals(userId)) {
            throw new RuntimeException("No tienes permisos para eliminar esta mentoría");
        }

        // Verificar que no tenga participantes activos
        boolean hasActiveParticipants = mentoria.getParticipants() != null &&
                mentoria.getParticipants().stream()
                        .anyMatch(p -> p.getStatus() == ParticipantStatus.INSCRITO);

        if (hasActiveParticipants) {
            throw new RuntimeException("No se puede eliminar una mentoría con participantes activos");
        }

        mentoriaRepository.delete(mentoria);
    }

    // OBTENER MENTORIAS RECOMENDADAS PARA UN USUARIO
    public List<MentoriaListResponse> getRecommendedMentorias(Long userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return mentoriaRepository.findAvailableMentoriasByInterests(user.getInterests())
                .stream()
                .map(this::convertToListResponse)
                .toList();
    }

    // Métodos de conversión
    private MentoriaRegisterResponse convertToRegisterResponse(Mentoria mentoria) {
        return new MentoriaRegisterResponse(
                mentoria.getId(),
                mentoria.getTitle(),
                mentoria.getDescription(),
                mentoria.getMaxPersons(),
                new MentorDto(
                        mentoria.getMentor().getId(),
                        mentoria.getMentor().getName(),
                        mentoria.getMentor().getSecondName(),
                        mentoria.getMentor().getEmail()
                ),
                mentoria.getInterests(),
                mentoria.getStatus(),
                mentoria.getCurrentParticipants(),
                mentoria.getCreationDate()
        );
    }

    private MentoriaUpdateResponse convertToUpdateResponse(Mentoria mentoria) {
        return new MentoriaUpdateResponse(
                mentoria.getId(),
                mentoria.getTitle(),
                mentoria.getDescription(),
                mentoria.getMaxPersons(),
                mentoria.getInterests(),
                mentoria.getStatus(),
                mentoria.getCreationDate()
        );
    }

    private MentoriaListResponse convertToListResponse(Mentoria mentoria) {
        return new MentoriaListResponse(
                mentoria.getId(),
                mentoria.getTitle(),
                mentoria.getDescription(),
                mentoria.getMentor().getName() + " " + mentoria.getMentor().getSecondName(),
                mentoria.getMaxPersons(),
                mentoria.getCurrentParticipants(),
                mentoria.getInterests(),
                mentoria.getStatus(),
                mentoria.getCreationDate(),
                mentoria.hasAvailableSpace()
        );
    }

    private MentoriaDetailResponse convertToDetailResponse(Mentoria mentoria) {
        List<ParticipantDto> participants = null;
        if (mentoria.getParticipants() != null) {
            participants = mentoria.getParticipants().stream()
                    .map(p -> new ParticipantDto(
                            p.getParticipant().getId(),
                            p.getParticipant().getName(),
                            p.getParticipant().getSecondName(),
                            p.getParticipant().getEmail(),
                            p.getRegistrationDate(),
                            p.getStatus().getValue()
                    ))
                    .toList();
        }

        return new MentoriaDetailResponse(
                mentoria.getId(),
                mentoria.getTitle(),
                mentoria.getDescription(),
                mentoria.getMaxPersons(),
                mentoria.getCurrentParticipants(),
                new MentorDto(
                        mentoria.getMentor().getId(),
                        mentoria.getMentor().getName(),
                        mentoria.getMentor().getSecondName(),
                        mentoria.getMentor().getEmail()
                ),
                mentoria.getInterests(),
                mentoria.getStatus(),
                mentoria.getCreationDate(),
                mentoria.hasAvailableSpace(),
                participants
        );
    }
}
