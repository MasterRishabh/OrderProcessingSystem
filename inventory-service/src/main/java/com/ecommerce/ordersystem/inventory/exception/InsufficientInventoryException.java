package com.ecommerce.ordersystem.inventory.exception;

public class InsufficientInventoryException extends RuntimeException {
    private final String itemCodeId;
    private final int requested;
    private final int available;

    public InsufficientInventoryException(String message) {
        super(message);
        this.itemCodeId = null;
        this.requested = 0;
        this.available = 0;
    }

    public InsufficientInventoryException(String itemCodeId, int requested, int available) {
        super(String.format("Insufficient inventory for item '%s': requested %d, available %d", itemCodeId, requested, available));
        this.itemCodeId = itemCodeId;
        this.requested = requested;
        this.available = available;
    }

    public String getItemCodeId() {
        return itemCodeId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
