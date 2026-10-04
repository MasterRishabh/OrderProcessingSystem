package com.ecommerce.ordersystem.inventory.repository;

import com.ecommerce.ordersystem.inventory.model.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {

    Optional<Item> findByCodeId(String codeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.codeId = :codeId")
    Optional<Item> findByCodeIdForUpdate(@Param("codeId") String codeId);

    /**
     * Atomic database update to reserve stock without race condition / overselling.
     * Returns 1 if successfully reserved, 0 if insufficient stock or item not found.
     */
    @Modifying
    @Query("UPDATE Item i SET i.quantity = i.quantity - :qty, i.updatedAt = :now WHERE i.codeId = :codeId AND i.quantity >= :qty")
    int reserveQuantity(@Param("codeId") String codeId, @Param("qty") int qty, @Param("now") Instant now);

    /**
     * Atomic database update to release/restore stock.
     */
    @Modifying
    @Query("UPDATE Item i SET i.quantity = i.quantity + :qty, i.updatedAt = :now WHERE i.codeId = :codeId")
    int releaseQuantity(@Param("codeId") String codeId, @Param("qty") int qty, @Param("now") Instant now);
}
