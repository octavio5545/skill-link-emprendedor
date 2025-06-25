package com.example.demo.mentoria.dto;

import com.example.demo.common.UserInterest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MentoriaRegisterRequest(
        @NotBlank(message = "El título de la mentoría es obligatorio")
        @Size(max = 100, message = "El título no puede exceder 100 caracteres")
        String title,

        @NotBlank(message = "La descripción de la mentoría es obligatoria")
        String description,

        @Positive(message = "El número máximo de personas debe ser positivo")
        Integer maxPersons,

        @NotNull(message = "Los intereses son obligatorios")
        @Size(min = 1, message = "Debe seleccionar al menos un interés")
        List<UserInterest> interests
) {}
