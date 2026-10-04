package com.ecommerce.ordersystem.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ItemDto {
    @NotBlank(message = "codeId must not be blank")
    private String codeId;

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotNull(message = "price must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "price must be positive")
    private BigDecimal price;

    @Min(value = 0, message = "quantity must be non-negative")
    private int quantity;

    public ItemDto() {
    }

    public ItemDto(String codeId, String name, BigDecimal price, int quantity) {
        this.codeId = codeId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getCodeId() {
        return codeId;
    }

    public void setCodeId(String codeId) {
        this.codeId = codeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
