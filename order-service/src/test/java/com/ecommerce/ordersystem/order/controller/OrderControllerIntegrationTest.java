package com.ecommerce.ordersystem.order.controller;

import com.ecommerce.ordersystem.common.dto.*;
import com.ecommerce.ordersystem.common.enums.OrderStatus;
import com.ecommerce.ordersystem.common.enums.PaymentStatus;
import com.ecommerce.ordersystem.order.client.InventoryClient;
import com.ecommerce.ordersystem.order.client.PaymentClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "order.status-transition.scheduler.enabled=false",
        "app.kafka.enabled=false"
})
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryClient inventoryClient;

    @MockBean
    private PaymentClient paymentClient;

    @Test
    @DisplayName("POST /api/orders should create order and return 201 Created")
    void testCreateOrder() throws Exception {
        when(inventoryClient.getItem("ITEM-101"))
                .thenReturn(new ItemDto("ITEM-101", "MacBook Pro", new BigDecimal("1999.99"), 10));

        when(inventoryClient.reserveInventory(any()))
                .thenReturn(new InventoryResponse(true, "Reserved", List.of()));

        when(paymentClient.processPayment(any()))
                .thenReturn(new ProcessPaymentResponse(true, "TXN-TEST", PaymentStatus.SUCCESS, "Charged", Instant.now()));

        OrderCreateRequest request = new OrderCreateRequest(
                "CUST-INT-1",
                List.of(new OrderItemRequest("ITEM-101", 1)),
                "CREDIT_CARD"
        );

        mockMvc.perform(post("/api/orders")
                        .header("Idempotency-Key", "KEY-ORDER-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(1999.99));
    }

    @Test
    @DisplayName("POST /api/orders should return 400 when request body has no items")
    void testCreateOrderEmptyItems() throws Exception {
        OrderCreateRequest request = new OrderCreateRequest("CUST-1", List.of(), "CREDIT_CARD");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET /api/orders should return list of orders")
    void testListOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status should update status respecting state transition rules")
    void testUpdateStatusFlow() throws Exception {
        // First create an order
        when(inventoryClient.getItem("ITEM-101"))
                .thenReturn(new ItemDto("ITEM-101", "MacBook Pro", new BigDecimal("1999.99"), 10));
        when(inventoryClient.reserveInventory(any()))
                .thenReturn(new InventoryResponse(true, "Reserved", List.of()));
        when(paymentClient.processPayment(any()))
                .thenReturn(new ProcessPaymentResponse(true, "TXN-TEST-2", PaymentStatus.SUCCESS, "Charged", Instant.now()));

        OrderCreateRequest request = new OrderCreateRequest(
                "CUST-FLOW",
                List.of(new OrderItemRequest("ITEM-101", 1)),
                "CREDIT_CARD"
        );

        String createResp = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(createResp).path("data").path("id").asText();

        // Advance to PROCESSING
        OrderStatusUpdateRequest update1 = new OrderStatusUpdateRequest(OrderStatus.PROCESSING, "Warehouse accepted");
        mockMvc.perform(put("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));

        // Advance to SHIPPED
        OrderStatusUpdateRequest update2 = new OrderStatusUpdateRequest(OrderStatus.SHIPPED, "Carrier picked up");
        mockMvc.perform(put("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));

        // Attempting to cancel now must fail with 400 Bad Request!
        mockMvc.perform(post("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ORDER_STATE"));
    }
}
