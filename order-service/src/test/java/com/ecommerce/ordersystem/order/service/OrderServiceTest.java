package com.ecommerce.ordersystem.order.service;

import com.ecommerce.ordersystem.common.dto.*;
import com.ecommerce.ordersystem.common.enums.OrderItemStatus;
import com.ecommerce.ordersystem.common.enums.OrderStatus;
import com.ecommerce.ordersystem.common.enums.PaymentStatus;
import com.ecommerce.ordersystem.order.client.InventoryClient;
import com.ecommerce.ordersystem.order.client.PaymentClient;
import com.ecommerce.ordersystem.order.event.OrderEventPublisher;
import com.ecommerce.ordersystem.order.exception.InvalidOrderStateException;
import com.ecommerce.ordersystem.order.exception.OrderProcessingException;
import com.ecommerce.ordersystem.order.model.Order;
import com.ecommerce.ordersystem.order.model.OrderItem;
import com.ecommerce.ordersystem.order.repository.OrderRepository;
import com.ecommerce.ordersystem.order.state.OrderStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private PaymentClient paymentClient;

    @Spy
    private OrderStateMachine orderStateMachine = new OrderStateMachine();

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private ItemDto itemDto1;
    private ItemDto itemDto2;

    @BeforeEach
    void setUp() {
        itemDto1 = new ItemDto("ITEM-101", "MacBook Pro", new BigDecimal("1999.99"), 10);
        itemDto2 = new ItemDto("ITEM-102", "Headphones", new BigDecimal("199.99"), 20);
    }

    @Test
    @DisplayName("Should create order successfully when inventory is available and payment succeeds")
    void testCreateOrderSuccess() {
        OrderCreateRequest request = new OrderCreateRequest(
                "CUST-1",
                List.of(new OrderItemRequest("ITEM-101", 1), new OrderItemRequest("ITEM-102", 2)),
                "CREDIT_CARD"
        );

        when(inventoryClient.getItem("ITEM-101")).thenReturn(itemDto1);
        when(inventoryClient.getItem("ITEM-102")).thenReturn(itemDto2);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("ORD-NEW");
            return o;
        });

        ProcessPaymentResponse paymentResponse = new ProcessPaymentResponse(
                true, "TXN-1", PaymentStatus.SUCCESS, "Charged", Instant.now()
        );
        when(paymentClient.processPayment(any())).thenReturn(paymentResponse);

        OrderResponse response = orderService.createOrder(request, "IDEMP-KEY-1");

        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("2399.97"), response.getTotalAmount());
        assertEquals(2, response.getItems().size());

        verify(inventoryClient, times(1)).reserveInventory(any());
        verify(paymentClient, times(1)).processPayment(any());
        verify(eventPublisher, times(1)).publishOrderCreated(any());
        verify(eventPublisher, times(1)).publishOrderConfirmed(any());
    }

    @Test
    @DisplayName("Should rollback inventory and mark PAYMENT_FAILED when payment fails")
    void testCreateOrderPaymentFailureCompensatesInventory() {
        OrderCreateRequest request = new OrderCreateRequest(
                "CUST-1",
                List.of(new OrderItemRequest("ITEM-101", 1)),
                "CREDIT_CARD"
        );

        when(inventoryClient.getItem("ITEM-101")).thenReturn(itemDto1);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("ORD-FAIL");
            return o;
        });

        ProcessPaymentResponse failedPayment = new ProcessPaymentResponse(
                false, "TXN-FAIL", PaymentStatus.FAILURE, "Declined", Instant.now()
        );
        when(paymentClient.processPayment(any())).thenReturn(failedPayment);

        OrderResponse response = orderService.createOrder(request, null);

        assertEquals(OrderStatus.PAYMENT_FAILED, response.getStatus());
        // Verify compensating inventory release was triggered!
        verify(inventoryClient, times(1)).releaseInventory(any());
        verify(eventPublisher, never()).publishOrderConfirmed(any());
    }

    @Test
    @DisplayName("Should return existing order on duplicate idempotency key")
    void testCreateOrderIdempotency() {
        Order existing = new Order("CUST-1", OrderStatus.PENDING, new BigDecimal("1999.99"), "IDEMP-DUP");
        existing.setId("ORD-EXISTING");
        when(orderRepository.findByIdempotencyKey("IDEMP-DUP")).thenReturn(Optional.of(existing));

        OrderCreateRequest request = new OrderCreateRequest("CUST-1", List.of(), "CREDIT_CARD");

        OrderResponse response = orderService.createOrder(request, "IDEMP-DUP");

        assertEquals("ORD-EXISTING", response.getId());
        verify(inventoryClient, never()).reserveInventory(any());
        verify(paymentClient, never()).processPayment(any());
    }

    @Test
    @DisplayName("Should cancel order completely when in PENDING status and restore inventory")
    void testCancelOrderSuccess() {
        Order order = new Order("CUST-1", OrderStatus.PENDING, new BigDecimal("1999.99"), null);
        order.setId("ORD-TO-CANCEL");
        OrderItem item = new OrderItem("ITEM-101", "MacBook Pro", 1, new BigDecimal("1999.99"), OrderItemStatus.ACTIVE);
        item.setId("ITEM-ROW-1");
        order.addItem(item);

        when(orderRepository.findByIdWithLock("ORD-TO-CANCEL")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.cancelOrder("ORD-TO-CANCEL");

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertEquals(OrderItemStatus.CANCELLED, response.getItems().get(0).getStatus());

        verify(inventoryClient, times(1)).releaseInventory(any());
        verify(paymentClient, times(1)).refundPayment(eq("ORD-TO-CANCEL"), eq(new BigDecimal("1999.99")));
        verify(eventPublisher, times(1)).publishOrderCancelled(any());
    }

    @Test
    @DisplayName("Should disallow cancellation when order is in PROCESSING status")
    void testCancelOrderDisallowedWhenProcessing() {
        Order order = new Order("CUST-1", OrderStatus.PROCESSING, new BigDecimal("1999.99"), null);
        order.setId("ORD-PROCESSING");
        when(orderRepository.findByIdWithLock("ORD-PROCESSING")).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder("ORD-PROCESSING"));
        verify(inventoryClient, never()).releaseInventory(any());
    }

    @Test
    @DisplayName("Should cancel individual item partially and restore partial inventory and amount")
    void testCancelOrderItemPartial() {
        Order order = new Order("CUST-1", OrderStatus.PENDING, new BigDecimal("2399.97"), null);
        order.setId("ORD-MULTI");
        OrderItem item1 = new OrderItem("ITEM-101", "MacBook Pro", 1, new BigDecimal("1999.99"), OrderItemStatus.ACTIVE);
        item1.setId("ITEM-1");
        OrderItem item2 = new OrderItem("ITEM-102", "Headphones", 2, new BigDecimal("199.99"), OrderItemStatus.ACTIVE);
        item2.setId("ITEM-2");
        order.addItem(item1);
        order.addItem(item2);

        when(orderRepository.findByIdWithLock("ORD-MULTI")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.cancelOrderItem("ORD-MULTI", "ITEM-2");

        // Order remains PENDING with updated total (2399.97 - 399.98 = 1999.99)
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("1999.99"), response.getTotalAmount());
        assertEquals(OrderItemStatus.CANCELLED, response.getItems().stream().filter(i -> i.getId().equals("ITEM-2")).findFirst().get().getStatus());

        verify(inventoryClient, times(1)).releaseInventory(any());
        verify(paymentClient, times(1)).refundPayment(eq("ORD-MULTI"), eq(new BigDecimal("399.98")));
        verify(eventPublisher, times(1)).publishOrderPartiallyCancelled(any());
    }
}
