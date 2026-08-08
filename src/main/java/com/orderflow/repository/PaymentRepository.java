package com.orderflow.repository;

import com.orderflow.entity.Payment;
import com.orderflow.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    boolean existsByOrderIdAndStatus(
            Long orderId,
            PaymentStatus status
    );
}