package com.ecommerce.ordersystem.common.event;

import java.time.Instant;

public class InventoryUpdatedEvent {
    private String eventId;
    private String itemCodeId;
    private int quantityChange;
    private int newAvailableQuantity;
    private String reason;
    private Instant timestamp;

    public InventoryUpdatedEvent() {
    }

    public InventoryUpdatedEvent(String eventId, String itemCodeId, int quantityChange, int newAvailableQuantity,
                                 String reason, Instant timestamp) {
        this.eventId = eventId;
        this.itemCodeId = itemCodeId;
        this.quantityChange = quantityChange;
        this.newAvailableQuantity = newAvailableQuantity;
        this.reason = reason;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getItemCodeId() {
        return itemCodeId;
    }

    public void setItemCodeId(String itemCodeId) {
        this.itemCodeId = itemCodeId;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    public int getNewAvailableQuantity() {
        return newAvailableQuantity;
    }

    public void setNewAvailableQuantity(int newAvailableQuantity) {
        this.newAvailableQuantity = newAvailableQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
