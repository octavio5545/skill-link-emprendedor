package com.example.demo.common;

public enum MentoriaStatus {
    DISPONIBLE("Disponible"),
    EN_PROGRESO("En Progreso"),
    COMPLETADA("Completada");

    private final String value;

    MentoriaStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
