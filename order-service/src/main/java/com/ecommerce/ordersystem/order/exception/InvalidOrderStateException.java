package com.ecommerce.ordersystem.order.exception;

import com.ecommerce.ordersystem.common.enums.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String orderId, OrderStatus currentStatus, String attemptedAction) {
        super(String.format("Cannot %s order '%s' in current status '%s'", attemptedAction, orderId, currentStatus));
    }

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
