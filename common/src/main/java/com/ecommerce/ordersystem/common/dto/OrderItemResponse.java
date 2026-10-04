package com.ecommerce.ordersystem.common.dto;

import com.ecommerce.ordersystem.common.enums.OrderItemStatus;
import java.math.BigDecimal;

public class OrderItemResponse {
    private String id;
    private String itemCodeId;
    private String itemName;
    private int quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal subtotal;
    private OrderItemStatus status;

    public OrderItemResponse() {
    }

    public OrderItemResponse(String id, String itemCodeId, String itemName, int quantity,
                             BigDecimal pricePerUnit, BigDecimal subtotal, OrderItemStatus status) {
        this.id = id;
        this.itemCodeId = itemCodeId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.subtotal = subtotal;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public OrderItemStatus getStatus() {
        return status;
    }

    public void setStatus(OrderItemStatus status) {
        this.status = status;
    }
}
