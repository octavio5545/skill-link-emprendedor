package com.example.demo.mentoria.dto;

import com.example.demo.common.MentoriaStatus;
import com.example.demo.common.UserInterest;

import java.time.OffsetDateTime;
import java.util.List;

public record MentoriaListResponse(
        Long id,
        String title,
        String description,
        String mentorName,
        Integer maxPersons,
        int currentParticipants,
        List<UserInterest> interests,
        MentoriaStatus status,
        OffsetDateTime creationDate,
        boolean hasAvailableSpace
) {}