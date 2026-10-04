package com.ecommerce.ordersystem.payment.controller;

import com.ecommerce.ordersystem.common.dto.*;
import com.ecommerce.ordersystem.payment.model.PaymentTransaction;
import com.ecommerce.ordersystem.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Gateway Adapter API", description = "Endpoints for payment processing, refund, and runtime simulator configuration")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    @Operation(summary = "Process payment for an order")
    public ResponseEntity<ApiResponse<ProcessPaymentResponse>> processPayment(
            @RequestBody(
                    description = "Payment processing request",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProcessPaymentRequest.class),
                            examples = @ExampleObject(
                                    name = "Payment Process Example",
                                    value = "{\n  \"orderId\": \"ORD-DEMO-001\",\n  \"amount\": 2898.00,\n  \"paymentMethod\": \"CREDIT_CARD\",\n  \"idempotencyKey\": \"IDEMP-PAY-1001\"\n}"
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody ProcessPaymentRequest request) {
        ProcessPaymentResponse response = paymentService.processPayment(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.ok("Payment processed successfully", response));
        } else {
            return ResponseEntity.ok(ApiResponse.ok("Payment processing failed", response));
        }
    }

    @PostMapping("/refund")
    @Operation(summary = "Refund payment for an order")
    public ResponseEntity<ApiResponse<ProcessPaymentResponse>> refundPayment(
            @Parameter(description = "Order ID", example = "ORD-DEMO-001")
            @RequestParam("orderId") String orderId,
            @Parameter(description = "Amount to refund", example = "399.00")
            @RequestParam("amount") BigDecimal amount) {
        ProcessPaymentResponse response = paymentService.refundPayment(orderId, amount);
        return ResponseEntity.ok(ApiResponse.ok("Refund processed successfully", response));
    }

    @GetMapping("/config")
    @Operation(summary = "Get current simulated payment gateway configuration")
    public ResponseEntity<ApiResponse<PaymentConfigDto>> getConfig() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getConfig()));
    }

    @PutMapping("/config")
    @Operation(summary = "Update runtime configuration of simulated payment gateway (toggle SUCCESS/FAILURE with zero downtime)")
    public ResponseEntity<ApiResponse<PaymentConfigDto>> updateConfig(
            @RequestBody(
                    description = "Simulator configuration payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentConfigDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Configure Success",
                                            summary = "Set gateway to return SUCCESS",
                                            value = "{\n  \"result\": \"SUCCESS\",\n  \"delayMs\": 0\n}"
                                    ),
                                    @ExampleObject(
                                            name = "Configure Failure (Test Circuit Breaker)",
                                            summary = "Set gateway to return FAILURE",
                                            value = "{\n  \"result\": \"FAILURE\",\n  \"delayMs\": 0\n}"
                                    )
                            }
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody PaymentConfigDto configDto) {
        PaymentConfigDto updated = paymentService.updateConfig(configDto);
        return ResponseEntity.ok(ApiResponse.ok("Payment gateway configuration updated", updated));
    }

    @GetMapping("/transactions/{orderId}")
    @Operation(summary = "Get payment transaction history for an order")
    public ResponseEntity<ApiResponse<List<PaymentTransaction>>> getTransactions(
            @Parameter(description = "Order ID", example = "ORD-DEMO-001")
            @PathVariable("orderId") String orderId) {
        List<PaymentTransaction> txns = paymentService.getTransactionsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.ok(txns));
    }
}
