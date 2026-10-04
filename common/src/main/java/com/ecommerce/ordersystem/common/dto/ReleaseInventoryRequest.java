package com.ecommerce.ordersystem.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class ReleaseInventoryRequest {
    private String orderId;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<ReleaseItemRequest> items = new ArrayList<>();

    public ReleaseInventoryRequest() {
    }

    public ReleaseInventoryRequest(String orderId, List<ReleaseItemRequest> items) {
        this.orderId = orderId;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<ReleaseItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ReleaseItemRequest> items) {
        this.items = items;
    }
}
