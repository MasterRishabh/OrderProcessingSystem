package com.ecommerce.ordersystem.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class OrderItemRequest {
    @NotBlank(message = "itemCodeId is required")
    private String itemCodeId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    public OrderItemRequest() {
    }

    public OrderItemRequest(String itemCodeId, int quantity) {
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
