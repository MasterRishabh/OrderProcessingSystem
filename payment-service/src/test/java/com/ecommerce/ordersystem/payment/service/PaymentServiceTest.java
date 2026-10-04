package com.ecommerce.ordersystem.payment.service;

import com.ecommerce.ordersystem.common.dto.PaymentConfigDto;
import com.ecommerce.ordersystem.common.dto.ProcessPaymentRequest;
import com.ecommerce.ordersystem.common.dto.ProcessPaymentResponse;
import com.ecommerce.ordersystem.common.enums.PaymentStatus;
import com.ecommerce.ordersystem.payment.gateway.PaymentGateway;
import com.ecommerce.ordersystem.payment.gateway.PaymentGatewayConfig;
import com.ecommerce.ordersystem.payment.model.PaymentTransaction;
import com.ecommerce.ordersystem.payment.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentGatewayConfig gatewayConfig;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @InjectMocks
    private PaymentService paymentService;

    private ProcessPaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = new ProcessPaymentRequest("ORD-1", new BigDecimal("199.99"), "CREDIT_CARD", "IDEMP-123");
    }

    @Test
    @DisplayName("Should successfully process payment when gateway returns SUCCESS")
    void testProcessPaymentSuccess() {
        when(transactionRepository.findByIdempotencyKey("IDEMP-123")).thenReturn(Optional.empty());

        ProcessPaymentResponse gatewayResponse = new ProcessPaymentResponse(
                true, "TXN-001", PaymentStatus.SUCCESS, "Success", Instant.now()
        );
        when(paymentGateway.processPayment(paymentRequest)).thenReturn(gatewayResponse);

        ProcessPaymentResponse response = paymentService.processPayment(paymentRequest);

        assertTrue(response.isSuccess());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("TXN-001", response.getTransactionId());
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    @DisplayName("Should return failure when gateway returns FAILURE")
    void testProcessPaymentFailure() {
        when(transactionRepository.findByIdempotencyKey("IDEMP-123")).thenReturn(Optional.empty());

        ProcessPaymentResponse gatewayResponse = new ProcessPaymentResponse(
                false, "TXN-002", PaymentStatus.FAILURE, "Declined", Instant.now()
        );
        when(paymentGateway.processPayment(paymentRequest)).thenReturn(gatewayResponse);

        ProcessPaymentResponse response = paymentService.processPayment(paymentRequest);

        assertFalse(response.isSuccess());
        assertEquals(PaymentStatus.FAILURE, response.getStatus());
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    @DisplayName("Should return existing transaction on duplicate request with same idempotency key")
    void testProcessPaymentIdempotency() {
        PaymentTransaction existingTxn = new PaymentTransaction(
                "ORD-1", new BigDecimal("199.99"), PaymentStatus.SUCCESS, "TXN-EXISTING", "IDEMP-123", "CREDIT_CARD", "Charged"
        );
        when(transactionRepository.findByIdempotencyKey("IDEMP-123")).thenReturn(Optional.of(existingTxn));

        ProcessPaymentResponse response = paymentService.processPayment(paymentRequest);

        assertTrue(response.isSuccess());
        assertEquals("TXN-EXISTING", response.getTransactionId());
        // Verify payment gateway was NOT invoked again!
        verify(paymentGateway, never()).processPayment(any());
    }

    @Test
    @DisplayName("Should update runtime configuration")
    void testUpdateConfig() {
        when(gatewayConfig.getDefaultResult()).thenReturn("FAILURE");
        when(gatewayConfig.getSimulatedDelayMs()).thenReturn(100L);

        PaymentConfigDto update = new PaymentConfigDto("FAILURE", 100L);
        PaymentConfigDto result = paymentService.updateConfig(update);

        verify(gatewayConfig, times(1)).setDefaultResult("FAILURE");
        verify(gatewayConfig, times(1)).setSimulatedDelayMs(100L);
        assertEquals("FAILURE", result.getDefaultResult());
    }
}
