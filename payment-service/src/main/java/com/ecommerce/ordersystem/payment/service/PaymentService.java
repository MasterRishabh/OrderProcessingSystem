package com.ecommerce.ordersystem.payment.service;

import com.ecommerce.ordersystem.common.dto.PaymentConfigDto;
import com.ecommerce.ordersystem.common.dto.ProcessPaymentRequest;
import com.ecommerce.ordersystem.common.dto.ProcessPaymentResponse;
import com.ecommerce.ordersystem.payment.gateway.PaymentGateway;
import com.ecommerce.ordersystem.payment.gateway.PaymentGatewayConfig;
import com.ecommerce.ordersystem.payment.model.PaymentTransaction;
import com.ecommerce.ordersystem.payment.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentGateway paymentGateway;
    private final PaymentGatewayConfig gatewayConfig;
    private final PaymentTransactionRepository transactionRepository;

    public PaymentService(PaymentGateway paymentGateway,
                          PaymentGatewayConfig gatewayConfig,
                          PaymentTransactionRepository transactionRepository) {
        this.paymentGateway = paymentGateway;
        this.gatewayConfig = gatewayConfig;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment for orderId={}, amount={}, idempotencyKey={}",
                request.getOrderId(), request.getAmount(), request.getIdempotencyKey());

        // Idempotency check: if transaction exists with this idempotency key, return cached result
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<PaymentTransaction> existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                PaymentTransaction txn = existing.get();
                log.info("Returning existing payment transaction for idempotencyKey={}", request.getIdempotencyKey());
                return new ProcessPaymentResponse(
                        txn.getStatus() == com.ecommerce.ordersystem.common.enums.PaymentStatus.SUCCESS,
                        txn.getTransactionId(),
                        txn.getStatus(),
                        "Idempotent response: " + txn.getMessage(),
                        txn.getCreatedAt()
                );
            }
        }

        // Delegate to adapter
        ProcessPaymentResponse response = paymentGateway.processPayment(request);

        // Record transaction
        PaymentTransaction transaction = new PaymentTransaction(
                request.getOrderId(),
                request.getAmount(),
                response.getStatus(),
                response.getTransactionId(),
                request.getIdempotencyKey(),
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "SIMULATED_CARD",
                response.getMessage()
        );
        transactionRepository.save(transaction);

        return response;
    }

    @Transactional
    public ProcessPaymentResponse refundPayment(String orderId, BigDecimal amount) {
        log.info("Refunding payment for orderId={}, amount={}", orderId, amount);
        ProcessPaymentResponse response = paymentGateway.refundPayment(orderId, amount);

        PaymentTransaction transaction = new PaymentTransaction(
                orderId,
                amount,
                response.getStatus(),
                response.getTransactionId(),
                null,
                "REFUND",
                response.getMessage()
        );
        transactionRepository.save(transaction);

        return response;
    }

    public List<PaymentTransaction> getTransactionsByOrderId(String orderId) {
        return transactionRepository.findByOrderId(orderId);
    }

    public PaymentConfigDto getConfig() {
        return new PaymentConfigDto(gatewayConfig.getDefaultResult(), gatewayConfig.getSimulatedDelayMs());
    }

    public PaymentConfigDto updateConfig(PaymentConfigDto update) {
        if (update.getDefaultResult() != null) {
            gatewayConfig.setDefaultResult(update.getDefaultResult());
        }
        if (update.getSimulatedDelayMs() >= 0) {
            gatewayConfig.setSimulatedDelayMs(update.getSimulatedDelayMs());
        }
        log.info("Updated payment gateway runtime config: result={}, delay={}ms",
                gatewayConfig.getDefaultResult(), gatewayConfig.getSimulatedDelayMs());
        return getConfig();
    }
}
