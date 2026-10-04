package com.ecommerce.ordersystem.order.scheduler;

import com.ecommerce.ordersystem.common.enums.OrderStatus;
import com.ecommerce.ordersystem.order.model.Order;
import com.ecommerce.ordersystem.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderStatusScheduler scheduler;

    @Test
    @DisplayName("Should advance all PENDING orders to PROCESSING")
    void testAdvancePendingOrdersToProcessing() {
        Order order1 = new Order("CUST-1", OrderStatus.PENDING, new BigDecimal("100.00"), null);
        order1.setId("ORD-1");
        Order order2 = new Order("CUST-2", OrderStatus.PENDING, new BigDecimal("200.00"), null);
        order2.setId("ORD-2");

        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(order1, order2));

        scheduler.advancePendingOrdersToProcessing();

        assertEquals(OrderStatus.PROCESSING, order1.getStatus());
        assertEquals(OrderStatus.PROCESSING, order2.getStatus());
        verify(orderRepository, times(1)).save(order1);
        verify(orderRepository, times(1)).save(order2);
    }

    @Test
    @DisplayName("Should handle empty pending orders gracefully")
    void testAdvancePendingOrdersEmpty() {
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of());

        scheduler.advancePendingOrdersToProcessing();

        verify(orderRepository, never()).save(any());
    }
}
