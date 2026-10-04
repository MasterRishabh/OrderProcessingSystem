package com.ecommerce.ordersystem.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProcessPaymentRequest {
    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount must be positive")
    private BigDecimal amount;

    private String paymentMethod;
    private String idempotencyKey;

    public ProcessPaymentRequest() {
    }

    public ProcessPaymentRequest(String orderId, BigDecimal amount, String paymentMethod, String idempotencyKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.idempotencyKey = idempotencyKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
