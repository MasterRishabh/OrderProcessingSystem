package com.ecommerce.ordersystem.common.dto;

import com.ecommerce.ordersystem.common.enums.PaymentStatus;
import java.time.Instant;

public class ProcessPaymentResponse {
    private boolean success;
    private String transactionId;
    private PaymentStatus status;
    private String message;
    private Instant processedAt;

    public ProcessPaymentResponse() {
    }

    public ProcessPaymentResponse(boolean success, String transactionId, PaymentStatus status, String message, Instant processedAt) {
        this.success = success;
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.processedAt = processedAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
