package com.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "productName is required")
        String productName,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be positive")
        Integer quantity,

        @NotNull(message = "price is required")
        @Positive(message = "price must be positive")
        BigDecimal price
) {
}
