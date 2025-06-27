package com.example.demo.postulacion.dto;

import com.example.demo.common.ApplicationStatus;

import java.time.OffsetDateTime;

public record PostulacionResponse(
        Long id,
        Long proyectoId,
        Long participanteId,
        OffsetDateTime fechaPostulacion,
        OffsetDateTime fechaRespuesta,
        ApplicationStatus estado
){
}
