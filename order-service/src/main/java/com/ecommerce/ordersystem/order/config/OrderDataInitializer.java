package com.ecommerce.ordersystem.order.config;

import com.ecommerce.ordersystem.common.enums.OrderItemStatus;
import com.ecommerce.ordersystem.common.enums.OrderStatus;
import com.ecommerce.ordersystem.order.model.Order;
import com.ecommerce.ordersystem.order.model.OrderItem;
import com.ecommerce.ordersystem.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Initializes a sample demo order (ORD-DEMO-001) on startup.
 * Enables immediate out-of-the-box testing across the complete lifecycle
 * (view order -> track order -> update status -> cancel) directly in Swagger UI.
 */
@Component
public class OrderDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OrderDataInitializer.class);
    private final OrderRepository orderRepository;

    public OrderDataInitializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (!orderRepository.existsById("ORD-DEMO-001")) {
            Order demoOrder = new Order("CUST-ALICE", OrderStatus.PENDING, new BigDecimal("2898.00"), "IDEMP-SWAGGER-DEMO-001");
            demoOrder.setId("ORD-DEMO-001");

            OrderItem item1 = new OrderItem("ITEM-101", "MacBook Pro 16\"", 1, new BigDecimal("2499.00"), OrderItemStatus.ACTIVE);
            item1.setId("ITEM-DEMO-001");

            OrderItem item2 = new OrderItem("ITEM-102", "Sony WH-1000XM5 Headphones", 1, new BigDecimal("399.00"), OrderItemStatus.ACTIVE);
            item2.setId("ITEM-DEMO-002");

            demoOrder.addItem(item1);
            demoOrder.addItem(item2);

            orderRepository.save(demoOrder);
            log.info("Initialized default demo order [ORD-DEMO-001] with items [ITEM-DEMO-001, ITEM-DEMO-002] for Swagger UI interactive testing");
        }
    }
}
