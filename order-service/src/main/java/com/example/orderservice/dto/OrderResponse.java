package com.example.orderservice.dto;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        Long userId,
        String productName,
        Integer quantity,
        BigDecimal price,
        String status
) {
}
