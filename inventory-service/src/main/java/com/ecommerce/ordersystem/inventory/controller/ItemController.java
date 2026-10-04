package com.ecommerce.ordersystem.inventory.controller;

import com.ecommerce.ordersystem.common.dto.*;
import com.ecommerce.ordersystem.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@Tag(name = "Item & Inventory API", description = "Endpoints for item catalog and stock reservation/release")
public class ItemController {

    private final InventoryService inventoryService;

    public ItemController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "List all items in catalog (pre-seeded with ITEM-101, ITEM-102, ITEM-103, ITEM-104, ITEM-SINGLE)")
    public ResponseEntity<ApiResponse<List<ItemDto>>> getAllItems() {
        List<ItemDto> items = inventoryService.getAllItems();
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @GetMapping("/{codeId}")
    @Operation(summary = "Get item by code")
    public ResponseEntity<ApiResponse<ItemDto>> getItem(
            @Parameter(description = "Item Code ID", example = "ITEM-101")
            @PathVariable("codeId") String codeId) {
        ItemDto item = inventoryService.getItem(codeId);
        return ResponseEntity.ok(ApiResponse.ok(item));
    }

    @PostMapping
    @Operation(summary = "Create or update item")
    public ResponseEntity<ApiResponse<ItemDto>> createOrUpdateItem(
            @RequestBody(
                    description = "New item payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemDto.class),
                            examples = @ExampleObject(
                                    name = "Sample Item Creation",
                                    value = "{\n  \"codeId\": \"ITEM-201\",\n  \"name\": \"Wireless Gaming Mouse\",\n  \"price\": 79.99,\n  \"quantity\": 40\n}"
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody ItemDto itemDto) {
        ItemDto saved = inventoryService.createOrUpdateItem(itemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Item saved successfully", saved));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve inventory items for an order")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveInventory(
            @RequestBody(
                    description = "Inventory reservation payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReserveInventoryRequest.class),
                            examples = @ExampleObject(
                                    name = "Reserve Stock Example",
                                    value = "{\n  \"orderId\": \"ORD-DEMO-001\",\n  \"items\": [\n    {\n      \"itemCodeId\": \"ITEM-101\",\n      \"quantity\": 1\n    }\n  ]\n}"
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody ReserveInventoryRequest request) {
        InventoryResponse response = inventoryService.reserveItems(request);
        return ResponseEntity.ok(ApiResponse.ok("Inventory reserved successfully", response));
    }

    @PostMapping("/release")
    @Operation(summary = "Release inventory items back to stock")
    public ResponseEntity<ApiResponse<InventoryResponse>> releaseInventory(
            @RequestBody(
                    description = "Inventory release payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReleaseInventoryRequest.class),
                            examples = @ExampleObject(
                                    name = "Release Stock Example",
                                    value = "{\n  \"orderId\": \"ORD-DEMO-001\",\n  \"items\": [\n    {\n      \"itemCodeId\": \"ITEM-101\",\n      \"quantity\": 1\n    }\n  ]\n}"
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody ReleaseInventoryRequest request) {
        InventoryResponse response = inventoryService.releaseItems(request);
        return ResponseEntity.ok(ApiResponse.ok("Inventory released successfully", response));
    }
}
