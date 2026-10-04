package com.ecommerce.ordersystem.order.model;

import com.ecommerce.ordersystem.common.enums.OrderItemStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "item_code_id", nullable = false)
    private String itemCodeId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price_per_unit", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderItemStatus status;

    public OrderItem() {
    }

    public OrderItem(String itemCodeId, String itemName, int quantity, BigDecimal pricePerUnit, OrderItemStatus status) {
        this.itemCodeId = itemCodeId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    public BigDecimal getSubtotal() {
        return pricePerUnit.multiply(BigDecimal.valueOf(quantity));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getItemCodeId() {
        return itemCodeId;
    }

    public void setItemCodeId(String itemCodeId) {
        this.itemCodeId = itemCodeId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
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

    public OrderItemStatus getStatus() {
        return status;
    }

    public void setStatus(OrderItemStatus status) {
        this.status = status;
    }
}
