package com.example.orderservice.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        String productName,
        Integer quantity,
        BigDecimal price
) {
}
