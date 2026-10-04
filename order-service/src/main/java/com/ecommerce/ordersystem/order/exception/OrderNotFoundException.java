package com.ecommerce.ordersystem.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("Order not found with id: " + orderId);
    }
}
