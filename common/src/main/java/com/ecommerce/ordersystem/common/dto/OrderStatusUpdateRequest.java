package com.ecommerce.ordersystem.common.dto;

import com.ecommerce.ordersystem.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateRequest {
    @NotNull(message = "status is required")
    private OrderStatus status;

    private String reason;

    public OrderStatusUpdateRequest() {
    }

    public OrderStatusUpdateRequest(OrderStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
