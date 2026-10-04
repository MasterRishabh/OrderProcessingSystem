package com.ecommerce.ordersystem.order.state;

import com.ecommerce.ordersystem.common.enums.OrderStatus;
import com.ecommerce.ordersystem.order.exception.InvalidOrderStateException;
import org.springframework.stereotype.Component;

@Component
public class OrderStateMachine {

    /**
     * Validates if transition from currentStatus to targetStatus is allowed.
     */
    public void validateTransition(String orderId, OrderStatus currentStatus, OrderStatus targetStatus) {
        if (!currentStatus.isValidTransition(targetStatus)) {
            throw new InvalidOrderStateException(
                    String.format("Invalid transition for order '%s': cannot transition from '%s' to '%s'",
                            orderId, currentStatus, targetStatus)
            );
        }
    }

    /**
     * Validates that order is eligible for cancellation (must be in PENDING status).
     */
    public void validateCancellationEligibility(String orderId, OrderStatus currentStatus) {
        if (!currentStatus.canBeCancelled()) {
            throw new InvalidOrderStateException(orderId, currentStatus, "cancel");
        }
    }
}
