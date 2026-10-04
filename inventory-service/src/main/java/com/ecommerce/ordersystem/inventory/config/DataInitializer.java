package com.ecommerce.ordersystem.inventory.config;

import com.ecommerce.ordersystem.inventory.model.Item;
import com.ecommerce.ordersystem.inventory.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ItemRepository itemRepository;

    public DataInitializer(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args) {
        if (itemRepository.count() == 0) {
            log.info("Seeding initial inventory items...");
            itemRepository.saveAll(List.of(
                    new Item("ITEM-101", "MacBook Pro M3", new BigDecimal("1999.99"), 10),
                    new Item("ITEM-102", "Wireless Noise-Cancelling Headphones", new BigDecimal("199.99"), 25),
                    new Item("ITEM-103", "Mechanical Gaming Keyboard", new BigDecimal("129.50"), 15),
                    new Item("ITEM-104", "Ultra-wide 4K Monitor", new BigDecimal("499.00"), 5),
                    new Item("ITEM-SINGLE", "Limited Edition Rare Collectible", new BigDecimal("5000.00"), 1)
            ));
            log.info("Seeded 5 sample items into inventory.");
        }
    }
}
