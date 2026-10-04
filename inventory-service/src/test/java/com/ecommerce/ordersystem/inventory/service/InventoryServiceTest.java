package com.ecommerce.ordersystem.inventory.service;

import com.ecommerce.ordersystem.common.dto.ItemDto;
import com.ecommerce.ordersystem.common.dto.ReleaseInventoryRequest;
import com.ecommerce.ordersystem.common.dto.ReleaseItemRequest;
import com.ecommerce.ordersystem.common.dto.ReserveInventoryRequest;
import com.ecommerce.ordersystem.common.dto.ReserveItemRequest;
import com.ecommerce.ordersystem.inventory.exception.InsufficientInventoryException;
import com.ecommerce.ordersystem.inventory.exception.ItemNotFoundException;
import com.ecommerce.ordersystem.inventory.model.Item;
import com.ecommerce.ordersystem.inventory.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item("ITEM-101", "MacBook Pro", new BigDecimal("1999.99"), 10);
    }

    @Test
    @DisplayName("Should successfully reserve inventory when sufficient stock is available")
    void testReserveInventorySuccess() {
        when(itemRepository.findByCodeId("ITEM-101")).thenReturn(Optional.of(sampleItem));
        when(itemRepository.reserveQuantity(eq("ITEM-101"), eq(2), any(Instant.class))).thenReturn(1);
        when(itemRepository.findAll()).thenReturn(List.of(sampleItem));

        ReserveInventoryRequest request = new ReserveInventoryRequest("ORD-1", List.of(new ReserveItemRequest("ITEM-101", 2)));

        var response = inventoryService.reserveItems(request);

        assertTrue(response.isSuccess());
        verify(itemRepository, times(1)).reserveQuantity(eq("ITEM-101"), eq(2), any(Instant.class));
    }

    @Test
    @DisplayName("Should throw InsufficientInventoryException when item quantity is not enough")
    void testReserveInventoryInsufficientQuantity() {
        when(itemRepository.findByCodeId("ITEM-101")).thenReturn(Optional.of(sampleItem));
        when(itemRepository.reserveQuantity(eq("ITEM-101"), eq(15), any(Instant.class))).thenReturn(0);

        ReserveInventoryRequest request = new ReserveInventoryRequest("ORD-1", List.of(new ReserveItemRequest("ITEM-101", 15)));

        assertThrows(InsufficientInventoryException.class, () -> inventoryService.reserveItems(request));
    }

    @Test
    @DisplayName("Should throw ItemNotFoundException when item code does not exist")
    void testReserveInventoryItemNotFound() {
        when(itemRepository.findByCodeId("NON-EXISTENT")).thenReturn(Optional.empty());

        ReserveInventoryRequest request = new ReserveInventoryRequest("ORD-1", List.of(new ReserveItemRequest("NON-EXISTENT", 1)));

        assertThrows(ItemNotFoundException.class, () -> inventoryService.reserveItems(request));
    }

    @Test
    @DisplayName("Should release inventory successfully")
    void testReleaseInventorySuccess() {
        when(itemRepository.findByCodeId("ITEM-101")).thenReturn(Optional.of(sampleItem));
        when(itemRepository.releaseQuantity(eq("ITEM-101"), eq(2), any(Instant.class))).thenReturn(1);
        when(itemRepository.findAll()).thenReturn(List.of(sampleItem));

        ReleaseInventoryRequest request = new ReleaseInventoryRequest("ORD-1", List.of(new ReleaseItemRequest("ITEM-101", 2)));

        var response = inventoryService.releaseItems(request);

        assertTrue(response.isSuccess());
        verify(itemRepository, times(1)).releaseQuantity(eq("ITEM-101"), eq(2), any(Instant.class));
    }

    @Test
    @DisplayName("Should get item by code")
    void testGetItem() {
        when(itemRepository.findByCodeId("ITEM-101")).thenReturn(Optional.of(sampleItem));

        ItemDto result = inventoryService.getItem("ITEM-101");

        assertNotNull(result);
        assertEquals("ITEM-101", result.getCodeId());
        assertEquals("MacBook Pro", result.getName());
    }
}
