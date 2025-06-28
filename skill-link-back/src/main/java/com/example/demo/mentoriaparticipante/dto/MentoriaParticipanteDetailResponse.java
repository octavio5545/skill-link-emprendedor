package com.example.demo.mentoriaparticipante.dto;

import com.example.demo.common.ParticipantStatus;
import com.example.demo.mentoria.dto.ParticipantDto;

import java.time.OffsetDateTime;

public record MentoriaParticipanteDetailResponse(
        Long id,
        MentoriaDto mentoria,
        ParticipantDto participant,
        OffsetDateTime registrationDate,
        ParticipantStatus status,
        boolean isActive
){}
