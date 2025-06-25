package com.example.demo.mentoriaparticipante.model;


import com.example.demo.common.ParticipantStatus;
import com.example.demo.mentoria.model.Mentoria;
import com.example.demo.user.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mentoria_participantes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"mentoria_id", "participante_id"}))
public class MentoriaParticipante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoria_id", nullable = false)
    private Mentoria mentoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id", nullable = false)
    private User participant;

    @Column(name = "fecha_inscripcion", columnDefinition = "datetime")
    @CreationTimestamp
    private OffsetDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private ParticipantStatus status = ParticipantStatus.INSCRITO;

    public MentoriaParticipante() {}

    public MentoriaParticipante(Mentoria mentoria, User participant) {
        this.mentoria = mentoria;
        this.participant = participant;
    }

    // Métodos de utilidad
    public boolean isInscrito() {
        return this.status == ParticipantStatus.INSCRITO;
    }

    public boolean isCompleted() {
        return this.status == ParticipantStatus.COMPLETADO;
    }

    public boolean hasAbandoned() {
        return status == ParticipantStatus.ABANDONADO;
    }

    public void markAsCompleted() {
        this.status = ParticipantStatus.COMPLETADO;
    }

    public void markAsAbandoned() {
        this.status = ParticipantStatus.ABANDONADO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Mentoria getMentoria() {
        return mentoria;
    }

    public void setMentoria(Mentoria mentoria) {
        this.mentoria = mentoria;
    }

    public User getParticipant() {
        return participant;
    }

    public void setParticipant(User participant) {
        this.participant = participant;
    }

    public OffsetDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(OffsetDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public ParticipantStatus getStatus() {
        return status;
    }

    public void setStatus(ParticipantStatus status) {
        this.status = status;
    }
}
