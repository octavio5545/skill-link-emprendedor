package com.example.demo.mentoria.model;

import com.example.demo.common.MentoriaStatus;
import com.example.demo.common.ParticipantStatus;
import com.example.demo.common.UserInterest;
import com.example.demo.mentoriaparticipante.model.MentoriaParticipante;
import com.example.demo.user.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "mentorias")
public class Mentoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "numero_personas_max")
    private Integer maxPersons;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Column(name = "fecha_creacion", columnDefinition = "datetime")
    @CreationTimestamp
    private OffsetDateTime creationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private MentoriaStatus status = MentoriaStatus.DISPONIBLE;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "mentoria_intereses", joinColumns = @JoinColumn(name = "mentoria_id"))
    @Column(name = "intereses", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<UserInterest> interests;

    @OneToMany(mappedBy = "mentoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MentoriaParticipante> participants;

    // Constructores
    public Mentoria() {}

    public Mentoria(String title, String description, Integer maxPersons, User mentor, List<UserInterest> interests) {
        this.title = title;
        this.description = description;
        this.maxPersons = maxPersons;
        this.mentor = mentor;
        this.interests = interests;
    }

    // Métodos de utilidad
    public boolean hasAvailableSpace(){
        return maxPersons == null || participants.size() < maxPersons;
    }

    public int getCurrentParticipants() {
        return participants != null ?
                (int) participants.stream()
                        .filter(participant -> participant.getStatus() == ParticipantStatus.INSCRITO)
                        .count() : 0;
    }

    public boolean isAvailable() {
        return status == MentoriaStatus.DISPONIBLE && hasAvailableSpace();
    }

    public boolean isInProgress() {
        return status == MentoriaStatus.EN_PROGRESO;
    }

    public boolean isCompleted() {
        return status == MentoriaStatus.COMPLETADA;
    }


    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxPersons() {
        return maxPersons;
    }

    public void setMaxPeople(Integer maxPersons) {
        this.maxPersons = maxPersons;
    }

    public User getMentor() {
        return mentor;
    }

    public void setMentor(User mentor) {
        this.mentor = mentor;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public MentoriaStatus getStatus() {
        return status;
    }

    public void setStatus(MentoriaStatus status) {
        this.status = status;
    }

    public List<UserInterest> getInterests() {
        return interests;
    }

    public void setInterests(List<UserInterest> interests) {
        this.interests = interests;
    }

    public List<MentoriaParticipante> getParticipants() {
        return participants;
    }

    public void setParticipants(List<MentoriaParticipante> participants) {
        this.participants = participants;
    }
}
