package com.example.demo.mentoriaparticipante.controller;

import com.example.demo.mentoriaparticipante.dto.*;
import com.example.demo.mentoriaparticipante.service.MentoriaParticipanteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mentoria-participantes")
public class MentoriaParticipanteController {

    @Autowired
    private MentoriaParticipanteService participanteService;

    // Método auxiliar para obtener el ID del usuario autenticado
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.example.demo.user.model.User user) {
            return user.getId();
        }
        throw new RuntimeException("Usuario no autenticado");
    }

    // INSCRIBIRSE A MENTORIA
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollInMentoria(@Valid @RequestBody MentoriaParticipanteRegisterRequest request) {
        try {
            Long userId = getCurrentUserId();
            MentoriaParticipanteRegisterResponse response = participanteService.enrollInMentoria(request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // OBTENER PARTICIPACIÓN POR ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getParticipacionById(@PathVariable Long id) {
        try {
            return participanteService.getParticipacionById(id)
                    .map(participacion -> ResponseEntity.ok(participacion))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // OBTENER MIS PARTICIPACIONES
    @GetMapping("/my-participations")
    public ResponseEntity<?> getMyParticipaciones() {
        try {
            Long userId = getCurrentUserId();
            List<MentoriaParticipanteListResponse> participaciones =
                    participanteService.getParticipacionesByUser(userId);
            return ResponseEntity.ok(participaciones);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // OBTENER MIS PARTICIPACIONES ACTIVAS
    @GetMapping("/my-active-participations")
    public ResponseEntity<?> getMyActiveParticipaciones() {
        try {
            Long userId = getCurrentUserId();
            List<MentoriaParticipanteListResponse> participaciones =
                    participanteService.getActiveParticipacionesByUser(userId);
            return ResponseEntity.ok(participaciones);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // OBTENER PARTICIPANTES DE UNA MENTORIA (solo para mentores)
    @GetMapping("/mentoria/{mentoriaId}/participants")
    public ResponseEntity<?> getParticipantesByMentoria(@PathVariable Long mentoriaId) {
        try {
            Long userId = getCurrentUserId();
            List<MentoriaParticipanteDetailResponse> participantes =
                    participanteService.getParticipantesByMentoria(mentoriaId, userId);
            return ResponseEntity.ok(participantes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // ACTUALIZAR ESTADO DE PARTICIPACIÓN
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateParticipacionStatus(
            @PathVariable Long id,
            @Valid @RequestBody MentoriaParticipanteUpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            MentoriaParticipanteUpdateResponse response =
                    participanteService.updateParticipacionStatus(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // ABANDONAR MENTORIA
    @PostMapping("/mentoria/{mentoriaId}/leave")
    public ResponseEntity<?> leaveMentoria(@PathVariable Long mentoriaId) {
        try {
            Long userId = getCurrentUserId();
            participanteService.leaveMentoria(mentoriaId, userId);
            return ResponseEntity.ok(Map.of("message", "Has abandonado la mentoría exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // COMPLETAR PARTICIPACIÓN (solo mentores)
    @PostMapping("/mentoria/{mentoriaId}/complete/{participantId}")
    public ResponseEntity<?> completeParticipation(
            @PathVariable Long mentoriaId,
            @PathVariable Long participantId) {
        try {
            Long userId = getCurrentUserId();
            participanteService.completeMentoria(mentoriaId, participantId, userId);
            return ResponseEntity.ok(Map.of("message", "Participación marcada como completada"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // VERIFICAR SI ESTOY INSCRITO EN UNA MENTORIA
    @GetMapping("/mentoria/{mentoriaId}/enrolled")
    public ResponseEntity<?> isEnrolled(@PathVariable Long mentoriaId) {
        try {
            Long userId = getCurrentUserId();
            boolean isEnrolled = participanteService.isUserEnrolled(mentoriaId, userId);
            return ResponseEntity.ok(Map.of("isEnrolled", isEnrolled));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // OBTENER ESTADÍSTICAS DE MENTOR
    @GetMapping("/mentor-stats")
    public ResponseEntity<?> getMentorStats() {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> stats = participanteService.getMentorStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }
}