package com.ecommerce.ordersystem.order.controller;

import com.ecommerce.ordersystem.common.dto.*;
import com.ecommerce.ordersystem.common.enums.OrderStatus;
import com.ecommerce.ordersystem.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Processing API", description = "End-to-End Order Lifecycle: View Orders -> Create Order -> Track Order Status -> Cancellation")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(
            summary = "Journey Step 1: View all orders (or filter by status)",
            description = "Retrieve paginated orders. On startup, pre-seeded demo order ORD-DEMO-001 is already available for inspection."
    )
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(
            @Parameter(description = "Optional status filter (e.g. PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)", example = "PENDING")
            @RequestParam(value = "status", required = false) OrderStatus status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<OrderResponse> orders = orderService.getAllOrders(status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/{orderId}")
    @Operation(
            summary = "Journey Step 2: Track & view order details by ID",
            description = "Fetch live tracking details, subtotal amounts, timestamps, and item statuses for a specific order. Default example ORD-DEMO-001 works out-of-the-box."
    )
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @Parameter(description = "Order ID to track (pre-seeded with ORD-DEMO-001)", example = "ORD-DEMO-001")
            @PathVariable("orderId") String orderId) {
        OrderResponse orderResponse = orderService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok(orderResponse));
    }

    @PostMapping
    @Operation(
            summary = "Journey Step 3: Create a new multi-item order",
            description = "Creates a new order, atomically reserves stock in Inventory Service, and processes payment. The pre-filled example payload is fully working with seeded catalog items (ITEM-101 and ITEM-102)."
    )
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestBody(
                    description = "Working order creation payload with catalog items",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Complete Order Creation Example",
                                    summary = "Multi-item Order Request",
                                    value = "{\n  \"customerId\": \"CUST-ALICE\",\n  \"paymentMethod\": \"CREDIT_CARD\",\n  \"items\": [\n    {\n      \"itemCodeId\": \"ITEM-101\",\n      \"quantity\": 1\n    },\n    {\n      \"itemCodeId\": \"ITEM-102\",\n      \"quantity\": 2\n    }\n  ]\n}"
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody OrderCreateRequest request,
            @Parameter(description = "Optional idempotency key to prevent duplicate orders", example = "JOURNEY-IDEMP-KEY-101")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        OrderResponse orderResponse = orderService.createOrder(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Order created successfully", orderResponse));
    }

    @PutMapping("/{orderId}/status")
    @Operation(
            summary = "Journey Step 4: Advance & track order status",
            description = "Explicitly advances an order status along its valid state transitions (PENDING -> PROCESSING -> SHIPPED -> DELIVERED). Note that PENDING orders also advance automatically via the 5-minute background job."
    )
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @Parameter(description = "Order ID to advance (e.g., ORD-DEMO-001)", example = "ORD-DEMO-001")
            @PathVariable("orderId") String orderId,
            @RequestBody(
                    description = "Status transition request payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderStatusUpdateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Advance to PROCESSING",
                                            summary = "Move order to PROCESSING",
                                            value = "{\n  \"status\": \"PROCESSING\",\n  \"reason\": \"Payment verified and order accepted for fulfillment\"\n}"
                                    ),
                                    @ExampleObject(
                                            name = "Advance to SHIPPED",
                                            summary = "Move order to SHIPPED (with tracking)",
                                            value = "{\n  \"status\": \"SHIPPED\",\n  \"reason\": \"Dispatched via Express Courier (Tracking: EXP-998877)\"\n}"
                                    ),
                                    @ExampleObject(
                                            name = "Advance to DELIVERED",
                                            summary = "Move order to DELIVERED",
                                            value = "{\n  \"status\": \"DELIVERED\",\n  \"reason\": \"Successfully delivered to customer residence\"\n}"
                                    )
                            }
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody OrderStatusUpdateRequest request) {
        OrderResponse orderResponse = orderService.updateOrderStatus(orderId, request.getStatus(), request.getReason());
        return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully", orderResponse));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(
            summary = "Journey Step 5A: Cancel order (Permitted ONLY in PENDING status)",
            description = "Cancels the entire order and triggers an automated compensation saga to restore reserved quantities to inventory and issue a payment refund."
    )
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Parameter(description = "Order ID to cancel (must be in PENDING status)", example = "ORD-DEMO-001")
            @PathVariable("orderId") String orderId) {
        OrderResponse orderResponse = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled successfully", orderResponse));
    }

    @PostMapping("/{orderId}/items/{itemId}/cancel")
    @Operation(
            summary = "Journey Step 5B: Partially cancel item from order (Permitted ONLY in PENDING status)",
            description = "Cancels a specific item from an order in PENDING status, recalculates order total, and restores the item quantity back to inventory."
    )
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrderItem(
            @Parameter(description = "Order ID (must be in PENDING status)", example = "ORD-DEMO-001")
            @PathVariable("orderId") String orderId,
            @Parameter(description = "Order item ID to cancel", example = "ITEM-DEMO-002")
            @PathVariable("itemId") String itemId) {
        OrderResponse orderResponse = orderService.cancelOrderItem(orderId, itemId);
        return ResponseEntity.ok(ApiResponse.ok("Order item cancelled successfully", orderResponse));
    }
}
