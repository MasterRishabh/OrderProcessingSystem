package com.ecommerce.ordersystem.order.scheduler;

import com.ecommerce.ordersystem.common.enums.OrderStatus;
import com.ecommerce.ordersystem.order.model.Order;
import com.ecommerce.ordersystem.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "order.status-transition.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class OrderStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    private final OrderRepository orderRepository;

    public OrderStatusScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Requirement: Background job automatically updates PENDING orders to PROCESSING every 5 minutes.
     * Configurable via 'order.status-transition.fixed-delay-ms' (defaults to 300000ms = 5 minutes).
     */
    @Scheduled(fixedDelayString = "${order.status-transition.fixed-delay-ms:300000}", initialDelayString = "${order.status-transition.initial-delay-ms:10000}")
    @Transactional
    public void advancePendingOrdersToProcessing() {
        log.info("Running scheduled background job: Advancing PENDING orders to PROCESSING...");

        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        if (pendingOrders.isEmpty()) {
            log.info("No PENDING orders found to advance.");
            return;
        }

        int count = 0;
        Instant now = Instant.now();
        for (Order order : pendingOrders) {
            order.setStatus(OrderStatus.PROCESSING);
            order.setUpdatedAt(now);
            orderRepository.save(order);
            count++;
            log.info("Advanced order {} from PENDING to PROCESSING", order.getId());
        }

        log.info("Scheduled job completed: Successfully advanced {} orders to PROCESSING.", count);
    }
}
