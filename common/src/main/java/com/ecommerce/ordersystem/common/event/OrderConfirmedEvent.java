package com.ecommerce.ordersystem.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderConfirmedEvent {
    private String eventId;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String transactionId;
    private Instant confirmedAt;

    public OrderConfirmedEvent() {
    }

    public OrderConfirmedEvent(String eventId, String orderId, String customerId, BigDecimal amount,
                               String transactionId, Instant confirmedAt) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.confirmedAt = confirmedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
