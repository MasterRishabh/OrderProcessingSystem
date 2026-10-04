package com.ecommerce.ordersystem.order.client;

import com.ecommerce.ordersystem.common.dto.*;
import com.ecommerce.ordersystem.order.exception.OrderProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestClient restClient;

    public InventoryClient(@Value("${services.inventory.url:http://localhost:8081}") String inventoryUrl,
                           RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(inventoryUrl).build();
    }

    public ItemDto getItem(String codeId) {
        try {
            ApiResponse<ItemDto> response = restClient.get()
                    .uri("/api/items/{codeId}", codeId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<ItemDto>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
            throw new OrderProcessingException("Item not found in inventory: " + codeId);
        } catch (Exception e) {
            log.error("Failed to fetch item {} from inventory service: {}", codeId, e.getMessage());
            throw new OrderProcessingException("Inventory lookup failed for item: " + codeId + " (" + e.getMessage() + ")");
        }
    }

    public InventoryResponse reserveInventory(ReserveInventoryRequest request) {
        try {
            log.info("Calling inventory-service to reserve items for order: {}", request.getOrderId());
            ApiResponse<InventoryResponse> response = restClient.post()
                    .uri("/api/items/reserve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<InventoryResponse>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
            throw new OrderProcessingException("Empty response received from inventory reservation");
        } catch (Exception e) {
            log.warn("Inventory reservation call failed: {}", e.getMessage());
            throw new OrderProcessingException("Inventory reservation failed: " + e.getMessage());
        }
    }

    public void releaseInventory(ReleaseInventoryRequest request) {
        try {
            log.info("Calling inventory-service to release items for order: {}", request.getOrderId());
            restClient.post()
                    .uri("/api/items/release")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to release inventory for order {}: {}", request.getOrderId(), e.getMessage());
            // Logged as critical but not throwing to avoid masking primary business cancellation
        }
    }
}
