package com.example.notificationservice.dto;

import java.time.Instant;

public record NotificationResponse(Long id, Long userId, String type, String message, Instant createdAt) {
}
