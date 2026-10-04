package com.ecommerce.ordersystem.common.event;

import com.ecommerce.ordersystem.common.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderCreatedEvent {
    private String eventId;
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Instant createdAt;
    private List<OrderItemEventPayload> items = new ArrayList<>();

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(String eventId, String orderId, String customerId, BigDecimal totalAmount,
                             OrderStatus status, Instant createdAt, List<OrderItemEventPayload> items) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItemEventPayload> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEventPayload> items) {
        this.items = items;
    }

    public static class OrderItemEventPayload {
        private String itemCodeId;
        private int quantity;
        private BigDecimal pricePerUnit;

        public OrderItemEventPayload() {
        }

        public OrderItemEventPayload(String itemCodeId, int quantity, BigDecimal pricePerUnit) {
            this.itemCodeId = itemCodeId;
            this.quantity = quantity;
            this.pricePerUnit = pricePerUnit;
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

        public BigDecimal getPricePerUnit() {
            return pricePerUnit;
        }

        public void setPricePerUnit(BigDecimal pricePerUnit) {
            this.pricePerUnit = pricePerUnit;
        }
    }
}
