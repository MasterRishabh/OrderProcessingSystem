package com.ecommerce.ordersystem.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ReleaseItemRequest {
    @NotBlank(message = "codeId is required")
    private String codeId;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    public ReleaseItemRequest() {
    }

    public ReleaseItemRequest(String codeId, int quantity) {
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
