package com.example.demo.postulacion.dto;

import com.example.demo.common.ApplicationStatus;

import java.time.OffsetDateTime;

public record PostulacionesResponseDTO(
        Long id,
        Long projectId,
        String participantId,
        String participantName,
        OffsetDateTime applicationDate,
        OffsetDateTime responseDate,
        ApplicationStatus status
) {
}
