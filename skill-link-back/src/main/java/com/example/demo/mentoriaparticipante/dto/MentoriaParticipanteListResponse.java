package com.example.demo.mentoriaparticipante.dto;

import com.example.demo.common.ParticipantStatus;

import java.time.OffsetDateTime;

public record MentoriaParticipanteListResponse(
        Long id,
        String mentoriaTitle,
        String mentorName,
        OffsetDateTime registrationDate,
        ParticipantStatus status,
        boolean isActive
) {}
