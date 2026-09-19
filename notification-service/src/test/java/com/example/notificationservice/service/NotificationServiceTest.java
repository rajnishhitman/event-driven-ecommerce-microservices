package com.example.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notificationservice.client.UserWebClientService;
import com.example.notificationservice.dto.UserNotificationResponse;
import com.example.notificationservice.dto.UserResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserWebClientService userWebClientService;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userWebClientService);
    }

    @Test
    void getNotificationsForUserCombinesWebClientAndDatabase() {
        UserResponse user = new UserResponse(1L, "Rajnish", "rajnish@example.com");
        Notification stored = new Notification(1L, "USER_CREATED", "Welcome Rajnish", Instant.parse("2026-01-01T00:00:00Z"));
        when(userWebClientService.getUser(1L)).thenReturn(Mono.just(user));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(stored));

        StepVerifier.create(notificationService.getNotificationsForUser(1L))
                .assertNext((UserNotificationResponse response) -> {
                    assertThat(response.user()).isEqualTo(user);
                    assertThat(response.notifications()).hasSize(1);
                    assertThat(response.notifications().get(0).type()).isEqualTo("USER_CREATED");
                })
                .verifyComplete();
    }

    @Test
    void savePersistsNotification() {
        notificationService.save(1L, NotificationService.TYPE_ORDER_CREATED, "Order placed");
        verify(notificationRepository).save(org.mockito.ArgumentMatchers.any(Notification.class));
    }
}
