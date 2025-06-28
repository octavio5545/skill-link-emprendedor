package com.example.demo.mentoria.dto;

import com.example.demo.common.MentoriaStatus;
import com.example.demo.common.UserInterest;

import java.time.OffsetDateTime;
import java.util.List;

public record MentoriaRegisterResponse(
        Long id,
        String title,
        String description,
        Integer maxPersons,
        MentorDto mentor,
        List<UserInterest> interests,
        MentoriaStatus status,
        int currentParticipants,
        OffsetDateTime creationDate
) {
}
