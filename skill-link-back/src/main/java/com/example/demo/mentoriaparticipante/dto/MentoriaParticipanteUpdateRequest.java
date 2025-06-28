package com.example.demo.mentoriaparticipante.dto;

import com.example.demo.common.ParticipantStatus;
import com.example.demo.mentoriaparticipante.model.MentoriaParticipante;

public record MentoriaParticipanteUpdateRequest(
        ParticipantStatus status
) {
}
