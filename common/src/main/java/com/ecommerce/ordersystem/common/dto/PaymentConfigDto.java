package com.ecommerce.ordersystem.common.dto;

public class PaymentConfigDto {
    private String defaultResult; // SUCCESS or FAILURE
    private long simulatedDelayMs;

    public PaymentConfigDto() {
    }

    public PaymentConfigDto(String defaultResult, long simulatedDelayMs) {
        this.defaultResult = defaultResult;
        this.simulatedDelayMs = simulatedDelayMs;
    }

    public String getDefaultResult() {
        return defaultResult;
    }

    public void setDefaultResult(String defaultResult) {
        this.defaultResult = defaultResult;
    }

    public long getSimulatedDelayMs() {
        return simulatedDelayMs;
    }

    public void setSimulatedDelayMs(long simulatedDelayMs) {
        this.simulatedDelayMs = simulatedDelayMs;
    }
}
