package com.ecommerce.ordersystem.payment.gateway;

import com.ecommerce.ordersystem.common.dto.ProcessPaymentRequest;
import com.ecommerce.ordersystem.common.dto.ProcessPaymentResponse;
import com.ecommerce.ordersystem.common.enums.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class SimulatedPaymentGatewayAdapter implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGatewayAdapter.class);

    private final PaymentGatewayConfig config;

    public SimulatedPaymentGatewayAdapter(PaymentGatewayConfig config) {
        this.config = config;
    }

    @Override
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {
        long delay = config.getSimulatedDelayMs();
        if (delay > 0) {
            try {
                log.info("Simulating payment processing delay of {} ms", delay);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Payment delay interrupted", e);
            }
        }

        String result = config.getDefaultResult();
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if ("SUCCESS".equalsIgnoreCase(result)) {
            log.info("Payment SUCCESS for orderId={}, amount={}", request.getOrderId(), request.getAmount());
            return new ProcessPaymentResponse(
                    true,
                    txnId,
                    PaymentStatus.SUCCESS,
                    "Payment charged successfully via SimulatedGateway",
                    Instant.now()
            );
        } else {
            log.warn("Payment FAILURE configured for orderId={}, amount={}", request.getOrderId(), request.getAmount());
            return new ProcessPaymentResponse(
                    false,
                    txnId,
                    PaymentStatus.FAILURE,
                    "Payment declined by bank/issuer via SimulatedGateway",
                    Instant.now()
            );
        }
    }

    @Override
    public ProcessPaymentResponse refundPayment(String orderId, BigDecimal amount) {
        String refundTxnId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Refund SUCCESS for orderId={}, amount={}", orderId, amount);
        return new ProcessPaymentResponse(
                true,
                refundTxnId,
                PaymentStatus.REFUNDED,
                "Refund completed successfully via SimulatedGateway",
                Instant.now()
        );
    }
}
