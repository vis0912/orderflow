package com.orderflow.dto;

import com.orderflow.entity.PaymentMethod;
import com.orderflow.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String transactionReference,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}