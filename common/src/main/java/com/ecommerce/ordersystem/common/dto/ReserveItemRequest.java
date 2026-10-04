package com.ecommerce.ordersystem.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ReserveItemRequest {
    @NotBlank(message = "codeId is required")
    private String codeId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    public ReserveItemRequest() {
    }

    public ReserveItemRequest(String codeId, int quantity) {
        this.codeId = codeId;
        this.quantity = quantity;
    }

    public String getCodeId() {
        return codeId;
    }

    public void setCodeId(String codeId) {
        this.codeId = codeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
