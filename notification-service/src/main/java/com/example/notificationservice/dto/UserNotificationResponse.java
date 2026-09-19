package com.example.notificationservice.dto;

import java.util.List;

/**
 * Combines live user data from user-service (WebClient) with locally stored notifications.
 */
public record UserNotificationResponse(UserResponse user, List<NotificationResponse> notifications) {
}
