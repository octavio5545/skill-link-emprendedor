package com.example.demo.postulacion.dto;

import jakarta.validation.constraints.NotNull;

public record PostulacionesRequestDTO(
        @NotNull(message = "El id proyecto es obligatorio")
        Long projectId,

        @NotNull(message = "El ID del participante es obligatorio")
        Long participantId
) {
}
