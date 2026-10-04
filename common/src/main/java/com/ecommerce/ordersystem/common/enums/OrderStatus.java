package com.ecommerce.ordersystem.common.enums;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    PAYMENT_FAILED;

    public boolean canBeCancelled() {
        return this == PENDING;
    }

    public boolean isValidTransition(OrderStatus nextStatus) {
        if (this == nextStatus) {
            return true;
        }
        return switch (this) {
            case PENDING -> nextStatus == PROCESSING || nextStatus == CANCELLED || nextStatus == PAYMENT_FAILED;
            case PROCESSING -> nextStatus == SHIPPED;
            case SHIPPED -> nextStatus == DELIVERED;
            case DELIVERED, CANCELLED, PAYMENT_FAILED -> false;
        };
    }
}
