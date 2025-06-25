package com.example.demo.mentoria.dto;

import java.time.OffsetDateTime;

public record ParticipantDto(
        Long id,
        String name,
        String secondName,
        String email,
        OffsetDateTime registrationDate,
        String status
) {}
