package com.sanchit.payment_service.repository;

import com.sanchit.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByOrderId(UUID orderId);
}