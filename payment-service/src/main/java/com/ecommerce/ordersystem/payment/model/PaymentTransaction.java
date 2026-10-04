package com.ecommerce.ordersystem.payment.model;

import com.ecommerce.ordersystem.common.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payment_order_id", columnList = "order_id"),
        @Index(name = "idx_payment_idempotency_key", columnList = "idempotency_key", unique = true)
})
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PaymentTransaction() {
    }

    public PaymentTransaction(String orderId, BigDecimal amount, PaymentStatus status,
                              String transactionId, String idempotencyKey, String paymentMethod, String message) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.transactionId = transactionId;
        this.idempotencyKey = idempotencyKey;
        this.paymentMethod = paymentMethod;
        this.message = message;
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
