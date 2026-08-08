package com.orderflow.event;

import java.math.BigDecimal;

public record OrderCreatedItem(
        Long productId,
        Integer quantity,
        BigDecimal unitPrice
) {
}