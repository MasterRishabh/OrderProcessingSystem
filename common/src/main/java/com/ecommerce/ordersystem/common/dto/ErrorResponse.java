package com.ecommerce.ordersystem.common.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
    private String errorCode;
    private String message;
    private String correlationId;
    private Instant timestamp;
    private List<String> details;

    public ErrorResponse() {
        this.timestamp = Instant.now();
        this.details = new ArrayList<>();
    }

    public ErrorResponse(String errorCode, String message, String correlationId, Instant timestamp, List<String> details) {
        this.errorCode = errorCode;
        this.message = message;
        this.correlationId = correlationId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.details = details != null ? details : new ArrayList<>();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}
