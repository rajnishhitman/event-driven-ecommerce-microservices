package com.example.userservice.event;

/**
 * Kafka payload published after a user is persisted.
 * Keep events small and stable — they become a public contract for other services.
 */
public record UserCreatedEvent(Long userId, String name, String email) {
}
