package com.jobqueue.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateJobRequest {

    @NotBlank(message = "type is required, e.g. 'send-email' or 'resize-image'")
    private String type;

    // Any JSON object as a raw string - kept loose on purpose so any job type works
    private String payload;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
