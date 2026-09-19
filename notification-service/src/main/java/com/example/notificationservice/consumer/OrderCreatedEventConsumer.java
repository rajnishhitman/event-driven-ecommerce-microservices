package com.example.notificationservice.consumer;

import com.example.notificationservice.event.OrderCreatedEvent;
import com.example.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    private final NotificationService notificationService;

    public OrderCreatedEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "order-created",
            groupId = "notification-group",
            properties = {
                    "spring.json.value.default.type=com.example.notificationservice.event.OrderCreatedEvent"
            }
    )
    public void consume(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent orderId={} userId={}", event.orderId(), event.userId());
        String message = "Order %d placed: %s x%d for %s".formatted(
                event.orderId(), event.productName(), event.quantity(), event.price());
        notificationService.save(event.userId(), NotificationService.TYPE_ORDER_CREATED, message);
    }
}
