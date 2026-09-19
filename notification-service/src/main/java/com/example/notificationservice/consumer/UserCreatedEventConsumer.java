package com.example.notificationservice.consumer;

import com.example.notificationservice.event.UserCreatedEvent;
import com.example.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserCreatedEventConsumer.class);

    private final NotificationService notificationService;

    public UserCreatedEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * groupId = notification-group: all instances of this service share the same group,
     * so each message is processed by only one instance.
     *
     * spring.json.value.default.type tells JsonDeserializer which class to build
     * because producers do not send Java type headers.
     */
    @KafkaListener(
            topics = "user-created",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.example.notificationservice.event.UserCreatedEvent"
            }
    )
    public void consume(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent userId={} email={}", event.userId(), event.email());
        String message = "Welcome %s. Your account was created.".formatted(event.name());
        notificationService.save(event.userId(), NotificationService.TYPE_USER_CREATED, message);
    }
}
