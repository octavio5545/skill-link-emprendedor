package com.example.demo.postulacion.dto;

import com.example.demo.common.ApplicationStatus;

public class PostulacionRequestDTO {
    private Long participantId;
    private Long projectId;
    private ApplicationStatus status;

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
