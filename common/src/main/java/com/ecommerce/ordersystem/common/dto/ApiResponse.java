package com.ecommerce.ordersystem.common.dto;

import java.time.Instant;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String correlationId;
    private Instant timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    public ApiResponse(boolean success, String message, T data, String correlationId, Instant timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.correlationId = correlationId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Operation completed successfully", data, null, Instant.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null, null, Instant.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
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
}
