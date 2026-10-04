package com.ecommerce.ordersystem.order.client;

import com.ecommerce.ordersystem.common.dto.ApiResponse;
import com.ecommerce.ordersystem.common.dto.ProcessPaymentRequest;
import com.ecommerce.ordersystem.common.dto.ProcessPaymentResponse;
import com.ecommerce.ordersystem.common.enums.PaymentStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestClient restClient;

    public PaymentClient(@Value("${services.payment.url:http://localhost:8082}") String paymentUrl,
                         RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(paymentUrl).build();
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {
        log.info("Calling payment-service to process payment for order: {}", request.getOrderId());
        ApiResponse<ProcessPaymentResponse> response = restClient.post()
                .uri("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<ApiResponse<ProcessPaymentResponse>>() {});

        if (response != null && response.getData() != null) {
            return response.getData();
        }

        return new ProcessPaymentResponse(
                false,
                null,
                PaymentStatus.FAILURE,
                "Empty response from payment service",
                Instant.now()
        );
    }

    public ProcessPaymentResponse processPaymentFallback(ProcessPaymentRequest request, Throwable ex) {
        log.error("Circuit breaker triggered for payment-service on order {}. Error: {}",
                request.getOrderId(), ex.getMessage());
        return new ProcessPaymentResponse(
                false,
                "CIRCUIT-OPEN",
                PaymentStatus.FAILURE,
                "Payment gateway unavailable / circuit breaker open: " + ex.getMessage(),
                Instant.now()
        );
    }

    public void refundPayment(String orderId, BigDecimal amount) {
        try {
            log.info("Calling payment-service to refund order: {}, amount: {}", orderId, amount);
            restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/payments/refund")
                            .queryParam("orderId", orderId)
                            .queryParam("amount", amount)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to refund payment for order {}: {}", orderId, e.getMessage());
        }
    }
}
