package com.example.demo.mentoria.dto;

import com.example.demo.common.MentoriaStatus;
import com.example.demo.common.UserInterest;

import java.time.OffsetDateTime;
import java.util.List;

public record MentoriaDetailResponse(
        Long id,
        String title,
        String description,
        Integer maxPersons,
        int currentParticipants,
        MentorDto mentor,
        List<UserInterest> interests,
        MentoriaStatus status,
        OffsetDateTime creationDate,
        boolean hasAvailableSpace,
        List<ParticipantDto> participants
){
}
