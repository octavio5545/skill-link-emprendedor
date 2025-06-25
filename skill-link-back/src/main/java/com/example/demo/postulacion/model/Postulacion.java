package com.example.demo.postulacion.model;


import com.example.demo.common.ApplicationStatus;
import com.example.demo.proyectonegocio.model.ProyectoNegocio;
import com.example.demo.user.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "postulaciones",
uniqueConstraints = @UniqueConstraint(columnNames = {"proyecto_id", "participante_id"}))
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private ProyectoNegocio project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id", nullable = false)
    private User participant;

    @Column(name = "fecha_postulacion", columnDefinition = "datetime")
    @CreationTimestamp
    private OffsetDateTime applicationDate;

    @Column(name = "fecha_respuesta", columnDefinition = "datetime")
    private OffsetDateTime responseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private ApplicationStatus status = ApplicationStatus.EN_ESPERA;

    public Postulacion() {}

    public Postulacion(ProyectoNegocio project, User participant) {
        this.project = project;
        this.participant = participant;
    }

    // Métodos de utilidad
    public boolean isPending() {
        return status == ApplicationStatus.EN_ESPERA;
    }

    public boolean isApproved() {
        return status == ApplicationStatus.APROBADO;
    }

    public boolean isRejected() {
        return status == ApplicationStatus.RECHAZADO;
    }

    public void approve() {
        this.status = ApplicationStatus.APROBADO;
        this.responseDate = OffsetDateTime.now();
    }

    public void reject() {
        this.status = ApplicationStatus.RECHAZADO;
        this.responseDate = OffsetDateTime.now();
    }

    // Getters y Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProyectoNegocio getProject() {
        return project;
    }

    public void setProject(ProyectoNegocio project) {
        this.project = project;
    }

    public User getParticipant() {
        return participant;
    }

    public void setParticipant(User participant) {
        this.participant = participant;
    }

    public OffsetDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(OffsetDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public OffsetDateTime getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(OffsetDateTime responseDate) {
        this.responseDate = responseDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
