package com.example.demo.common;

public enum ApplicationStatus {

    EN_ESPERA("En Espera"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String value;

    ApplicationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
