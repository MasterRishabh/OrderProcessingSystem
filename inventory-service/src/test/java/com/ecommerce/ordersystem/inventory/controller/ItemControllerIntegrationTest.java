package com.ecommerce.ordersystem.inventory.controller;

import com.ecommerce.ordersystem.common.dto.ReleaseInventoryRequest;
import com.ecommerce.ordersystem.common.dto.ReleaseItemRequest;
import com.ecommerce.ordersystem.common.dto.ReserveInventoryRequest;
import com.ecommerce.ordersystem.common.dto.ReserveItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/items should return seeded items")
    void testGetAllItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/items/{codeId} should return item details")
    void testGetItemByCode() throws Exception {
        mockMvc.perform(get("/api/items/ITEM-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.codeId").value("ITEM-101"))
                .andExpect(jsonPath("$.data.name").value("MacBook Pro M3"));
    }

    @Test
    @DisplayName("POST /api/items/reserve should successfully reserve items")
    void testReserveInventory() throws Exception {
        ReserveInventoryRequest request = new ReserveInventoryRequest(
                "ORD-TEST-1",
                List.of(new ReserveItemRequest("ITEM-102", 2))
        );

        mockMvc.perform(post("/api/items/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/items/reserve should return 400 when insufficient inventory")
    void testReserveInventoryInsufficientStock() throws Exception {
        ReserveInventoryRequest request = new ReserveInventoryRequest(
                "ORD-TEST-2",
                List.of(new ReserveItemRequest("ITEM-SINGLE", 999))
        );

        mockMvc.perform(post("/api/items/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_INVENTORY"));
    }

    @Test
    @DisplayName("POST /api/items/release should restore stock")
    void testReleaseInventory() throws Exception {
        ReleaseInventoryRequest request = new ReleaseInventoryRequest(
                "ORD-TEST-1",
                List.of(new ReleaseItemRequest("ITEM-102", 2))
        );

        mockMvc.perform(post("/api/items/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
