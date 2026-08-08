package com.orderflow.dto;

import com.orderflow.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}