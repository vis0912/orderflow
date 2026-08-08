package com.orderflow.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stockQuantity,
        String description,
        Boolean active
) {
}