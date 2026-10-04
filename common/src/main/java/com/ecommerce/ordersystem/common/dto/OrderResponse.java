package com.ecommerce.ordersystem.common.dto;

import com.ecommerce.ordersystem.common.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderResponse {
    private String id;
    private String customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String idempotencyKey;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> items = new ArrayList<>();

    public OrderResponse() {
    }

    public OrderResponse(String id, String customerId, OrderStatus status, BigDecimal totalAmount,
                         String idempotencyKey, Instant createdAt, Instant updatedAt,
                         List<OrderItemResponse> items) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }
}
