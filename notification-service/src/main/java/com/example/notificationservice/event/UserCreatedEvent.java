package com.example.notificationservice.event;

public record UserCreatedEvent(Long userId, String name, String email) {
}
