package com.ecommerce.ordersystem.payment.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class PaymentGatewayConfig {

    private final AtomicReference<String> defaultResult;
    private final AtomicLong simulatedDelayMs;

    public PaymentGatewayConfig(
            @Value("${payment.simulated.result:SUCCESS}") String initialResult,
            @Value("${payment.simulated.delay-ms:0}") long initialDelay) {
        this.defaultResult = new AtomicReference<>(initialResult.toUpperCase());
        this.simulatedDelayMs = new AtomicLong(initialDelay);
    }

    public String getDefaultResult() {
        return defaultResult.get();
    }

    public void setDefaultResult(String result) {
        if (result != null && (result.equalsIgnoreCase("SUCCESS") || result.equalsIgnoreCase("FAILURE"))) {
            this.defaultResult.set(result.toUpperCase());
        } else {
            throw new IllegalArgumentException("Invalid payment result: must be 'SUCCESS' or 'FAILURE'");
        }
    }

    public long getSimulatedDelayMs() {
        return simulatedDelayMs.get();
    }

    public void setSimulatedDelayMs(long delayMs) {
        this.simulatedDelayMs.set(Math.max(0, delayMs));
    }
}
