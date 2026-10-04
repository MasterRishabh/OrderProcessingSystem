package com.ecommerce.ordersystem.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class OrderCreateRequest {
    @NotBlank(message = "customerId is required")
    private String customerId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items = new ArrayList<>();

    private String paymentMethod;

    public OrderCreateRequest() {
    }

    public OrderCreateRequest(String customerId, List<OrderItemRequest> items, String paymentMethod) {
        this.customerId = customerId;
        this.items = items;
        this.paymentMethod = paymentMethod;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
