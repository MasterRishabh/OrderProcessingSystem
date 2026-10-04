package com.ecommerce.ordersystem.order.event;

import com.ecommerce.ordersystem.common.event.OrderCancelledEvent;
import com.ecommerce.ordersystem.common.event.OrderConfirmedEvent;
import com.ecommerce.ordersystem.common.event.OrderCreatedEvent;
import com.ecommerce.ordersystem.common.event.OrderPartiallyCancelledEvent;

public interface OrderEventPublisher {
    void publishOrderCreated(OrderCreatedEvent event);
    void publishOrderConfirmed(OrderConfirmedEvent event);
    void publishOrderCancelled(OrderCancelledEvent event);
    void publishOrderPartiallyCancelled(OrderPartiallyCancelledEvent event);
}
