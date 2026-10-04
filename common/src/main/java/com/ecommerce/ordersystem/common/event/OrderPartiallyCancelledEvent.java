package com.ecommerce.ordersystem.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderPartiallyCancelledEvent {
    private String eventId;
    private String orderId;
    private String orderItemId;
    private String itemCodeId;
    private int cancelledQuantity;
    private BigDecimal refundedAmount;
    private BigDecimal newOrderTotal;
    private Instant cancelledAt;

    public OrderPartiallyCancelledEvent() {
    }

    public OrderPartiallyCancelledEvent(String eventId, String orderId, String orderItemId, String itemCodeId,
                                       int cancelledQuantity, BigDecimal refundedAmount, BigDecimal newOrderTotal,
                                       Instant cancelledAt) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.itemCodeId = itemCodeId;
        this.cancelledQuantity = cancelledQuantity;
        this.refundedAmount = refundedAmount;
        this.newOrderTotal = newOrderTotal;
        this.cancelledAt = cancelledAt;
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

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getItemCodeId() {
        return itemCodeId;
    }

    public void setItemCodeId(String itemCodeId) {
        this.itemCodeId = itemCodeId;
    }

    public int getCancelledQuantity() {
        return cancelledQuantity;
    }

    public void setCancelledQuantity(int cancelledQuantity) {
        this.cancelledQuantity = cancelledQuantity;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public BigDecimal getNewOrderTotal() {
        return newOrderTotal;
    }

    public void setNewOrderTotal(BigDecimal newOrderTotal) {
        this.newOrderTotal = newOrderTotal;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
