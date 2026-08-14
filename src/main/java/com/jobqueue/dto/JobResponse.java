package com.jobqueue.dto;

import com.jobqueue.model.Job;
import com.jobqueue.model.JobStatus;

import java.time.Instant;
import java.util.UUID;

public class JobResponse {

    private UUID id;
    private String type;
    private JobStatus status;
    private String result;
    private String errorMessage;
    private int attempts;
    private int maxAttempts;
    private Instant createdAt;
    private Instant updatedAt;

    public static JobResponse from(Job job) {
        JobResponse dto = new JobResponse();
        dto.id = job.getId();
        dto.type = job.getType();
        dto.status = job.getStatus();
        dto.result = job.getResult();
        dto.errorMessage = job.getErrorMessage();
        dto.attempts = job.getAttempts();
        dto.maxAttempts = job.getMaxAttempts();
        dto.createdAt = job.getCreatedAt();
        dto.updatedAt = job.getUpdatedAt();
        return dto;
    }

    // --- Getters (needed for Jackson serialization) ---

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public JobStatus getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
