package com.ecommerce.ordersystem.payment.controller;

import com.ecommerce.ordersystem.common.dto.PaymentConfigDto;
import com.ecommerce.ordersystem.common.dto.ProcessPaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/payments/process should process payment successfully by default")
    void testProcessPaymentSuccess() throws Exception {
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                "ORD-PAY-1", new BigDecimal("499.00"), "CREDIT_CARD", "KEY-1"
        );

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("PUT /api/payments/config should update runtime result to FAILURE and affect subsequent payments")
    void testRuntimeConfigChangeToFailure() throws Exception {
        // Set config to FAILURE
        PaymentConfigDto config = new PaymentConfigDto("FAILURE", 0);
        mockMvc.perform(put("/api/payments/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultResult").value("FAILURE"));

        // Process payment - should fail
        ProcessPaymentRequest request = new ProcessPaymentRequest(
                "ORD-PAY-FAIL", new BigDecimal("100.00"), "CREDIT_CARD", "KEY-FAIL"
        );

        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILURE"));

        // Reset config back to SUCCESS
        config.setDefaultResult("SUCCESS");
        mockMvc.perform(put("/api/payments/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(config)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultResult").value("SUCCESS"));
    }

    @Test
    @DisplayName("GET /api/payments/config should return current config")
    void testGetConfig() throws Exception {
        mockMvc.perform(get("/api/payments/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.defaultResult").exists());
    }
}
