package com.ecommerce.ordersystem.order.service;

import com.ecommerce.ordersystem.common.dto.*;
import com.ecommerce.ordersystem.common.enums.OrderItemStatus;
import com.ecommerce.ordersystem.common.enums.OrderStatus;
import com.ecommerce.ordersystem.common.enums.PaymentStatus;
import com.ecommerce.ordersystem.common.event.*;
import com.ecommerce.ordersystem.order.client.InventoryClient;
import com.ecommerce.ordersystem.order.client.PaymentClient;
import com.ecommerce.ordersystem.order.event.OrderEventPublisher;
import com.ecommerce.ordersystem.order.exception.InvalidOrderStateException;
import com.ecommerce.ordersystem.order.exception.OrderNotFoundException;
import com.ecommerce.ordersystem.order.exception.OrderProcessingException;
import com.ecommerce.ordersystem.order.model.Order;
import com.ecommerce.ordersystem.order.model.OrderItem;
import com.ecommerce.ordersystem.order.repository.OrderRepository;
import com.ecommerce.ordersystem.order.state.OrderStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final OrderStateMachine orderStateMachine;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        InventoryClient inventoryClient,
                        PaymentClient paymentClient,
                        OrderStateMachine orderStateMachine,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
        this.orderStateMachine = orderStateMachine;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates an order with multiple items, reserves inventory, and processes payment.
     * Protected with idempotency key and compensating rollback if payment fails.
     */
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, String idempotencyKey) {
        log.info("Creating order for customerId={}, items={}, idempotencyKey={}",
                request.getCustomerId(), request.getItems().size(), idempotencyKey);

        // 1. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Order> existing = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Returning existing order for idempotencyKey={}", idempotencyKey);
                return mapToResponse(existing.get());
            }
        }

        // 2. Item Lookup & Price Calculation
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<ReserveItemRequest> reserveRequests = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            ItemDto itemDto = inventoryClient.getItem(itemReq.getItemCodeId());
            if (itemDto == null) {
                throw new OrderProcessingException("Item not found: " + itemReq.getItemCodeId());
            }

            BigDecimal price = itemDto.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = new OrderItem(
                    itemReq.getItemCodeId(),
                    itemDto.getName(),
                    itemReq.getQuantity(),
                    price,
                    OrderItemStatus.ACTIVE
            );
            orderItems.add(orderItem);

            reserveRequests.add(new ReserveItemRequest(itemReq.getItemCodeId(), itemReq.getQuantity()));
        }

        // 3. Create Order Entity
        Order order = new Order(request.getCustomerId(), OrderStatus.PENDING, totalAmount, idempotencyKey);
        for (OrderItem oi : orderItems) {
            order.addItem(oi);
        }
        Order savedOrder = orderRepository.save(order);

        // 4. Reserve Inventory
        ReserveInventoryRequest reserveRequest = new ReserveInventoryRequest(savedOrder.getId(), reserveRequests);
        inventoryClient.reserveInventory(reserveRequest);

        // 5. Process Payment via Payment Client (Circuit Breaker protected)
        ProcessPaymentRequest paymentRequest = new ProcessPaymentRequest(
                savedOrder.getId(),
                totalAmount,
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "CREDIT_CARD",
                idempotencyKey != null ? "PAY-" + idempotencyKey : null
        );

        ProcessPaymentResponse paymentResponse = paymentClient.processPayment(paymentRequest);

        if (!paymentResponse.isSuccess()) {
            log.warn("Payment failed for order {}: {}. Performing compensating inventory release.",
                    savedOrder.getId(), paymentResponse.getMessage());

            // Compensating transaction: release reserved inventory
            List<ReleaseItemRequest> releaseRequests = reserveRequests.stream()
                    .map(r -> new ReleaseItemRequest(r.getCodeId(), r.getQuantity()))
                    .toList();
            inventoryClient.releaseInventory(new ReleaseInventoryRequest(savedOrder.getId(), releaseRequests));

            savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(savedOrder);

            return mapToResponse(savedOrder);
        }

        // Payment Success -> Confirm Order and publish events
        log.info("Order {} successfully created and confirmed with status PENDING", savedOrder.getId());

        // Publish OrderCreatedEvent
        OrderCreatedEvent createdEvent = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt(),
                savedOrder.getItems().stream()
                        .map(i -> new OrderCreatedEvent.OrderItemEventPayload(i.getItemCodeId(), i.getQuantity(), i.getPricePerUnit()))
                        .toList()
        );
        eventPublisher.publishOrderCreated(createdEvent);

        // Publish OrderConfirmedEvent
        OrderConfirmedEvent confirmedEvent = new OrderConfirmedEvent(
                UUID.randomUUID().toString(),
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount(),
                paymentResponse.getTransactionId(),
                Instant.now()
        );
        eventPublisher.publishOrderConfirmed(confirmedEvent);

        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return mapToResponse(order);
    }

    public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByStatus(status, pageable).map(this::mapToResponse);
        }
        return orderRepository.findAll(pageable).map(this::mapToResponse);
    }

    /**
     * Complete order cancellation. Allowed ONLY when order is in PENDING status.
     * Restores all item quantities back to inventory.
     */
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        log.info("Cancelling entire order: {}", orderId);
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validate cancellation rule: ONLY PENDING orders can be cancelled
        orderStateMachine.validateCancellationEligibility(orderId, order.getStatus());

        List<ReleaseItemRequest> releaseRequests = new ArrayList<>();
        List<OrderCancelledEvent.CancelledItemPayload> eventItems = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            if (item.getStatus() == OrderItemStatus.ACTIVE) {
                item.setStatus(OrderItemStatus.CANCELLED);
                releaseRequests.add(new ReleaseItemRequest(item.getItemCodeId(), item.getQuantity()));
                eventItems.add(new OrderCancelledEvent.CancelledItemPayload(item.getItemCodeId(), item.getQuantity()));
            }
        }

        // Release inventory back to stock
        if (!releaseRequests.isEmpty()) {
            inventoryClient.releaseInventory(new ReleaseInventoryRequest(orderId, releaseRequests));
        }

        // Process refund
        paymentClient.refundPayment(orderId, order.getTotalAmount());

        // Update state
        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);

        // Publish event
        OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(
                UUID.randomUUID().toString(),
                orderId,
                "Customer requested full cancellation",
                Instant.now(),
                eventItems
        );
        eventPublisher.publishOrderCancelled(cancelledEvent);

        log.info("Order {} successfully cancelled and inventory restored", orderId);
        return mapToResponse(updated);
    }

    /**
     * Partial order cancellation. Allowed ONLY when order is in PENDING status.
     * Restores cancelled item quantity back to inventory.
     */
    @Transactional
    public OrderResponse cancelOrderItem(String orderId, String itemId) {
        log.info("Cancelling item {} in order {}", itemId, orderId);
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validate cancellation rule: ONLY PENDING orders can be cancelled
        orderStateMachine.validateCancellationEligibility(orderId, order.getStatus());

        OrderItem targetItem = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new OrderProcessingException("Order item '" + itemId + "' not found in order '" + orderId + "'"));

        if (targetItem.getStatus() == OrderItemStatus.CANCELLED) {
            throw new InvalidOrderStateException("Item '" + itemId + "' is already cancelled");
        }

        // Mark item cancelled
        targetItem.setStatus(OrderItemStatus.CANCELLED);

        // Calculate refunded amount
        BigDecimal refundedAmount = targetItem.getSubtotal();
        BigDecimal newTotal = order.getTotalAmount().subtract(refundedAmount);
        order.setTotalAmount(newTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newTotal);

        // Restore inventory
        inventoryClient.releaseInventory(new ReleaseInventoryRequest(
                orderId,
                List.of(new ReleaseItemRequest(targetItem.getItemCodeId(), targetItem.getQuantity()))
        ));

        // Refund payment
        paymentClient.refundPayment(orderId, refundedAmount);

        // Check if all items are cancelled
        boolean allCancelled = order.getItems().stream().allMatch(i -> i.getStatus() == OrderItemStatus.CANCELLED);
        if (allCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        Order updated = orderRepository.save(order);

        // Publish event
        OrderPartiallyCancelledEvent partialEvent = new OrderPartiallyCancelledEvent(
                UUID.randomUUID().toString(),
                orderId,
                itemId,
                targetItem.getItemCodeId(),
                targetItem.getQuantity(),
                refundedAmount,
                order.getTotalAmount(),
                Instant.now()
        );
        eventPublisher.publishOrderPartiallyCancelled(partialEvent);

        log.info("Item {} in order {} cancelled. New order total={}", itemId, orderId, order.getTotalAmount());
        return mapToResponse(updated);
    }

    /**
     * Updates order status with state machine transition checks.
     */
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus, String reason) {
        log.info("Updating status for order {} to {}. Reason: {}", orderId, newStatus, reason);
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        orderStateMachine.validateTransition(orderId, order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(),
                        i.getItemCodeId(),
                        i.getItemName(),
                        i.getQuantity(),
                        i.getPricePerUnit(),
                        i.getSubtotal(),
                        i.getStatus()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getIdempotencyKey(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }
}
