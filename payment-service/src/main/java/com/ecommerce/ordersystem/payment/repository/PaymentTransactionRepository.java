package com.ecommerce.ordersystem.payment.repository;

import com.ecommerce.ordersystem.payment.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByOrderId(String orderId);
    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);
}
