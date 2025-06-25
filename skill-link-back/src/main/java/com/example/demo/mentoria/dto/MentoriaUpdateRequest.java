package com.example.demo.mentoria.dto;

import com.example.demo.common.MentoriaStatus;
import com.example.demo.common.UserInterest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MentoriaUpdateRequest(
        @Size(max = 100, message = "El título no puede exceder 100 caracteres")
        String title,

        String description,

        @Positive(message = "El número máximo de personas debe ser positivo")
        Integer maxPersons,

        List<UserInterest> interests,

        MentoriaStatus status
) {}
