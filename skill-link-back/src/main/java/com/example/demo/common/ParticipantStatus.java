package com.example.demo.common;

public enum ParticipantStatus {
    INSCRITO("Inscrito"),
    COMPLETADO("Completado"),
    ABANDONADO("Abandonado");

    private final String value;

    ParticipantStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
