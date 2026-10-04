package com.ecommerce.ordersystem.common.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderCancelledEvent {
    private String eventId;
    private String orderId;
    private String reason;
    private Instant cancelledAt;
    private List<CancelledItemPayload> items = new ArrayList<>();

    public OrderCancelledEvent() {
    }

    public OrderCancelledEvent(String eventId, String orderId, String reason, Instant cancelledAt, List<CancelledItemPayload> items) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.reason = reason;
        this.cancelledAt = cancelledAt;
        this.items = items != null ? items : new ArrayList<>();
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public List<CancelledItemPayload> getItems() {
        return items;
    }

    public void setItems(List<CancelledItemPayload> items) {
        this.items = items;
    }

    public static class CancelledItemPayload {
        private String itemCodeId;
        private int quantity;

        public CancelledItemPayload() {
        }

        public CancelledItemPayload(String itemCodeId, int quantity) {
            this.itemCodeId = itemCodeId;
            this.quantity = quantity;
        }

        public String getItemCodeId() {
            return itemCodeId;
        }

        public void setItemCodeId(String itemCodeId) {
            this.itemCodeId = itemCodeId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
