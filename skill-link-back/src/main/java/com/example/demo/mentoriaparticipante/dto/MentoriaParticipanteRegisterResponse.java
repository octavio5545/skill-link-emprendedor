package com.example.demo.mentoriaparticipante.dto;

import com.example.demo.common.ParticipantStatus;
import com.example.demo.mentoria.dto.ParticipantDto;

import java.time.OffsetDateTime;

public record MentoriaParticipanteRegisterResponse(
        Long id,
        MentoriaDto mentoria,
        ParticipantDto participant,
        OffsetDateTime registrationDate,
        ParticipantStatus status,
        String message
) {
}
