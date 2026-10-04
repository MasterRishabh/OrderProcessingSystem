package com.ecommerce.ordersystem.inventory.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String codeId) {
        super("Item not found with code: " + codeId);
    }
}
