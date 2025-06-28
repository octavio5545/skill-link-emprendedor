package com.example.demo.mentoriaparticipante.dto;

public record MentoriaDto(
        Long id,
        String title,
        String description,
        String mentorName,
        Integer maxPersons,
        int currentParticipants
) {
}
