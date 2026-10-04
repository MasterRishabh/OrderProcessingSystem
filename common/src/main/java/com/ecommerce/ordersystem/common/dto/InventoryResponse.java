package com.ecommerce.ordersystem.common.dto;

import java.util.ArrayList;
import java.util.List;

public class InventoryResponse {
    private boolean success;
    private String message;
    private List<ItemDto> items = new ArrayList<>();

    public InventoryResponse() {
    }

    public InventoryResponse(boolean success, String message, List<ItemDto> items) {
        this.success = success;
        this.message = message;
        this.items = items;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ItemDto> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> items) {
        this.items = items;
    }
}
