package com.example.demo.proyectonegocio.model;

import com.example.demo.common.ApplicationStatus;
import com.example.demo.postulacion.model.Postulacion;
import com.example.demo.user.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "proyecto_negocio")
public class ProyectoNegocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "numero_personas_max")
    private Integer maxPeople;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    private User creator;

    @Column(name = "fecha_creacion", columnDefinition = "datetime")
    @CreationTimestamp
    private OffsetDateTime creationDate;

    @Column(name = "fecha_modificacion", columnDefinition = "datetime")
    @UpdateTimestamp
    private OffsetDateTime modificationDate;

    @Column(name = "activo")
    private boolean active = true;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "proyecto_intereses", joinColumns = @JoinColumn(name = "proyecto_id"))
    @Column(name = "interes", nullable = false)
    @Enumerated(EnumType.STRING)
    private List<String> interests;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Postulacion> applications;

    // Constructores
    public ProyectoNegocio() {}

    public ProyectoNegocio(String title, String description, Integer maxPeople, User creator, List<String> interests) {
        this.title = title;
        this.description = description;
        this.maxPeople = maxPeople;
        this.creator = creator;
        this.interests = interests;
    }

    // Métodos de utilidad
    public int getApplicationsCount() {
        return applications != null ? applications.size() : 0;
    }

    public int getApprovedApplicationsCount() {
        return applications != null ?
                (int) applications.stream()
                .filter(application -> application.getStatus() == ApplicationStatus.APROBADO)
                .count() : 0;
    }

    public boolean hasAvailableSpace() {
        return maxPeople == null || getApplicationsCount() < maxPeople;
    }

    public boolean isActive() {
        return active;
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

    public Integer getMaxPeople() {
        return maxPeople;
    }

    public void setMaxPeople(Integer maxPeople) {
        this.maxPeople = maxPeople;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public OffsetDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(OffsetDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<Postulacion> getApplications() {
        return applications;
    }

    public void setApplications(List<Postulacion> applications) {
        this.applications = applications;
    }
}
