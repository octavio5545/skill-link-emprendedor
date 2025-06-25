package com.example.demo.mentoria.controller;

import com.example.demo.common.MentoriaStatus;
import com.example.demo.common.UserInterest;
import com.example.demo.mentoria.dto.*;
import com.example.demo.mentoria.service.MentoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mentorias")
public class MentoriaController {

    @Autowired
    private MentoriaService mentoriaService;

    // Método auxiliar para obtener el ID del usuario autenticado
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.example.demo.user.model.User user) {
            return user.getId();
        }
        throw new RuntimeException("Usuario no autenticado");
    }

    @PostMapping
    public ResponseEntity<?> createMentoria(@Valid @RequestBody MentoriaRegisterRequest request) {
        try {
            Long userId = getCurrentUserId();
            MentoriaRegisterResponse response = mentoriaService.createMentoria(request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @GetMapping
    public ResponseEntity<Page<MentoriaListResponse>> getAllMentorias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<MentoriaListResponse> mentorias = mentoriaService.getAllMentorias(page, size);
            return ResponseEntity.ok(mentorias);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // OBTENER MENTORIA POR ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getMentoriaById(@PathVariable Long id) {
        try {
            return mentoriaService.getMentoriaById(id)
                    .map(mentoria -> ResponseEntity.ok(mentoria))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // OBTENER MENTORIAS POR MENTOR
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<MentoriaListResponse>> getMentoriasByMentor(@PathVariable Long mentorId) {
        try {
            List<MentoriaListResponse> mentorias = mentoriaService.getMentoriasByMentor(mentorId);
            return ResponseEntity.ok(mentorias);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // OBTENER MIS MENTORIAS
    @GetMapping("/my-mentorias")
    public ResponseEntity<?> getMyMentorias() {
        try {
            Long userId = getCurrentUserId();
            List<MentoriaListResponse> mentorias = mentoriaService.getMentoriasByMentor(userId);
            return ResponseEntity.ok(mentorias);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // OBTENER MENTORIAS DISPONIBLES
    @GetMapping("/available")
    public ResponseEntity<List<MentoriaListResponse>> getAvailableMentorias() {
        try {
            List<MentoriaListResponse> mentorias = mentoriaService.getAvailableMentorias();
            return ResponseEntity.ok(mentorias);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // OBTENER MENTORIAS CON ESPACIO DISPONIBLE
    @GetMapping("/with-space")
    public ResponseEntity<List<MentoriaListResponse>> getMentoriasWithSpace() {
        try {
            List<MentoriaListResponse> mentorias = mentoriaService.getMentoriasWithAvailableSpace();
            return ResponseEntity.ok(mentorias);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // OBTENER MENTORIAS POR INTERÉS
    @GetMapping("/interest/{interest}")
    public ResponseEntity<List<MentoriaListResponse>> getMentoriasByInterest(@PathVariable UserInterest interest) {
        try {
            List<MentoriaListResponse> mentorias = mentoriaService.getMentoriasByInterest(interest);
            return ResponseEntity.ok(mentorias);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // BUSCAR MENTORIAS POR TÍTULO
    @GetMapping("/search")
    public ResponseEntity<List<MentoriaListResponse>> searchMentorias(@RequestParam String title) {
        try {
            List<MentoriaListResponse> mentorias = mentoriaService.searchMentoriasByTitle(title);
            return ResponseEntity.ok(mentorias);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // OBTENER MENTORIAS POPULARES
    @GetMapping("/popular")
    public ResponseEntity<List<MentoriaListResponse>> getMostPopularMentorias(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<MentoriaListResponse> mentorias = mentoriaService.getMostPopularMentorias(limit);
            return ResponseEntity.ok(mentorias);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // OBTENER MENTORIAS RECIENTES
    @GetMapping("/recent")
    public ResponseEntity<List<MentoriaListResponse>> getRecentMentorias(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<MentoriaListResponse> mentorias = mentoriaService.getRecentMentorias(limit);
            return ResponseEntity.ok(mentorias);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // OBTENER MENTORIAS RECOMENDADAS
    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedMentorias() {
        try {
            Long userId = getCurrentUserId();
            List<MentoriaListResponse> mentorias = mentoriaService.getRecommendedMentorias(userId);
            return ResponseEntity.ok(mentorias);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // ACTUALIZAR MENTORIA
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMentoria(
            @PathVariable Long id,
            @Valid @RequestBody MentoriaUpdateRequest request) {
        try {
            Long userId = getCurrentUserId();
            MentoriaUpdateResponse response = mentoriaService.updateMentoria(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // CAMBIAR ESTADO DE MENTORIA
    @PutMapping("/{id}/status")
    public ResponseEntity<?> changeMentoriaStatus(
            @PathVariable Long id,
            @RequestParam MentoriaStatus status) {
        try {
            Long userId = getCurrentUserId();
            mentoriaService.changeMentoriaStatus(id, status, userId);
            return ResponseEntity.ok(Map.of("message", "Estado de mentoría actualizado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // ELIMINAR MENTORIA
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMentoria(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            mentoriaService.deleteMentoria(id, userId);
            return ResponseEntity.ok(Map.of("message", "Mentoría eliminada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }
}