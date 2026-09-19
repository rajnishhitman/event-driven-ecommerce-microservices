package com.example.notificationservice.service;

import com.example.notificationservice.client.UserWebClientService;
import com.example.notificationservice.dto.NotificationResponse;
import com.example.notificationservice.dto.UserNotificationResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class NotificationService {

    public static final String TYPE_USER_CREATED = "USER_CREATED";
    public static final String TYPE_ORDER_CREATED = "ORDER_CREATED";

    private final NotificationRepository notificationRepository;
    private final UserWebClientService userWebClientService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserWebClientService userWebClientService) {
        this.notificationRepository = notificationRepository;
        this.userWebClientService = userWebClientService;
    }

    @Transactional
    public Notification save(Long userId, String type, String message) {
        return notificationRepository.save(new Notification(userId, type, message, Instant.now()));
    }

    /**
     * WebClient fetches user details; JPA reads stored notifications.
     * JPA is blocking, so the DB call runs on boundedElastic while the HTTP call stays reactive.
     */
    public Mono<UserNotificationResponse> getNotificationsForUser(Long userId) {
        return userWebClientService.getUser(userId)
                .zipWith(Mono.fromCallable(() -> notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                        .subscribeOn(Schedulers.boundedElastic()))
                .map(tuple -> new UserNotificationResponse(
                        tuple.getT1(),
                        tuple.getT2().stream().map(this::toResponse).toList()
                ));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getStoredNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getMessage(),
                notification.getCreatedAt()
        );
    }
}
