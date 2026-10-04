package com.ecommerce.ordersystem.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class ReserveInventoryRequest {
    private String orderId;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<ReserveItemRequest> items = new ArrayList<>();

    public ReserveInventoryRequest() {
    }

    public ReserveInventoryRequest(String orderId, List<ReserveItemRequest> items) {
        this.orderId = orderId;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<ReserveItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ReserveItemRequest> items) {
        this.items = items;
    }
}
