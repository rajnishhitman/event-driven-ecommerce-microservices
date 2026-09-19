package com.example.notificationservice.controller;

import com.example.notificationservice.dto.UserNotificationResponse;
import com.example.notificationservice.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Demonstrates WebClient: this endpoint always loads user details from user-service
     * over HTTP, then attaches notifications stored in notification_db.
     */
    @GetMapping("/user/{userId}")
    public Mono<UserNotificationResponse> getUserNotifications(@PathVariable Long userId) {
        return notificationService.getNotificationsForUser(userId);
    }
}
