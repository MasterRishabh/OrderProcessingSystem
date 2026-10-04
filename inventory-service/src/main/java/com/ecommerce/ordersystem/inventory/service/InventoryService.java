package com.ecommerce.ordersystem.inventory.service;

import com.ecommerce.ordersystem.common.dto.*;
import com.ecommerce.ordersystem.inventory.exception.InsufficientInventoryException;
import com.ecommerce.ordersystem.inventory.exception.ItemNotFoundException;
import com.ecommerce.ordersystem.inventory.model.Item;
import com.ecommerce.ordersystem.inventory.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final ItemRepository itemRepository;

    public InventoryService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<ItemDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public ItemDto getItem(String codeId) {
        return itemRepository.findByCodeId(codeId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ItemNotFoundException(codeId));
    }

    @Transactional
    public ItemDto createOrUpdateItem(ItemDto dto) {
        Item item = itemRepository.findByCodeId(dto.getCodeId())
                .orElse(new Item(dto.getCodeId(), dto.getName(), dto.getPrice(), dto.getQuantity()));

        item.setName(dto.getName());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());

        Item saved = itemRepository.save(item);
        log.info("Saved item {}: quantity={}", saved.getCodeId(), saved.getQuantity());
        return mapToDto(saved);
    }

    /**
     * Atomically reserves inventory for an order with deadlock prevention (sorting by codeId)
     * and concurrency protection via atomic decrement.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryResponse reserveItems(ReserveInventoryRequest request) {
        log.info("Attempting to reserve inventory for orderId={}", request.getOrderId());

        // Sort items by codeId to prevent deadlock across concurrent orders
        List<ReserveItemRequest> sortedItems = request.getItems().stream()
                .sorted(Comparator.comparing(ReserveItemRequest::getCodeId))
                .toList();

        // Validate and reserve each item atomically
        Instant now = Instant.now();
        for (ReserveItemRequest itemReq : sortedItems) {
            Item item = itemRepository.findByCodeId(itemReq.getCodeId())
                    .orElseThrow(() -> new ItemNotFoundException(itemReq.getCodeId()));

            int updatedRows = itemRepository.reserveQuantity(itemReq.getCodeId(), itemReq.getQuantity(), now);
            if (updatedRows == 0) {
                // Insufficient quantity or race condition
                log.warn("Failed to reserve {} units for item {}. Current stock={}",
                        itemReq.getQuantity(), itemReq.getCodeId(), item.getQuantity());
                throw new InsufficientInventoryException(itemReq.getCodeId(), itemReq.getQuantity(), item.getQuantity());
            }
            log.info("Successfully reserved {} units of item {} for order {}",
                    itemReq.getQuantity(), itemReq.getCodeId(), request.getOrderId());
        }

        return new InventoryResponse(true, "Inventory successfully reserved for order " + request.getOrderId(), getAllItems());
    }

    /**
     * Releases inventory back to available stock upon order or item cancellation.
     */
    @Transactional
    public InventoryResponse releaseItems(ReleaseInventoryRequest request) {
        log.info("Releasing inventory for orderId={}", request.getOrderId());

        Instant now = Instant.now();
        for (ReleaseItemRequest itemReq : request.getItems()) {
            Item item = itemRepository.findByCodeId(itemReq.getCodeId())
                    .orElseThrow(() -> new ItemNotFoundException(itemReq.getCodeId()));

            itemRepository.releaseQuantity(itemReq.getCodeId(), itemReq.getQuantity(), now);
            log.info("Released {} units back to item {} for order {}",
                    itemReq.getQuantity(), itemReq.getCodeId(), request.getOrderId());
        }

        return new InventoryResponse(true, "Inventory successfully released for order " + request.getOrderId(), getAllItems());
    }

    private ItemDto mapToDto(Item item) {
        return new ItemDto(item.getCodeId(), item.getName(), item.getPrice(), item.getQuantity());
    }
}
