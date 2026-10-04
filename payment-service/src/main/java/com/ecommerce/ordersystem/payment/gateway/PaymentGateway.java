package com.ecommerce.ordersystem.payment.gateway;

import com.ecommerce.ordersystem.common.dto.ProcessPaymentRequest;
import com.ecommerce.ordersystem.common.dto.ProcessPaymentResponse;

import java.math.BigDecimal;

/**
 * Adapter interface for external payment processors (Stripe, PayPal, Mock Gateway).
 */
public interface PaymentGateway {
    ProcessPaymentResponse processPayment(ProcessPaymentRequest request);
    ProcessPaymentResponse refundPayment(String orderId, BigDecimal amount);
}
