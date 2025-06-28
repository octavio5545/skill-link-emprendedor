package com.example.demo.mentoriaparticipante.dto;

import com.example.demo.common.ParticipantStatus;

import java.time.OffsetDateTime;

public record MentoriaParticipanteUpdateResponse(
        Long id,
        OffsetDateTime registrationDate,
        ParticipantStatus status,
        String message
) {
}
