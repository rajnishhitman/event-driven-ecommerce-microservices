package com.example.notificationservice.consumer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.notificationservice.event.OrderCreatedEvent;
import com.example.notificationservice.event.UserCreatedEvent;
import com.example.notificationservice.service.NotificationService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaConsumersTest {

    @Mock
    private NotificationService notificationService;

    @Test
    void userCreatedConsumerStoresWelcomeMessage() {
        UserCreatedEventConsumer consumer = new UserCreatedEventConsumer(notificationService);

        consumer.consume(new UserCreatedEvent(1L, "Rajnish", "rajnish@example.com"));

        verify(notificationService).save(
                eq(1L),
                eq(NotificationService.TYPE_USER_CREATED),
                eq("Welcome Rajnish. Your account was created."));
    }

    @Test
    void orderCreatedConsumerStoresOrderMessage() {
        OrderCreatedEventConsumer consumer = new OrderCreatedEventConsumer(notificationService);

        consumer.consume(new OrderCreatedEvent(10L, 1L, "Laptop", 2, new BigDecimal("75000")));

        verify(notificationService).save(
                eq(1L),
                eq(NotificationService.TYPE_ORDER_CREATED),
                eq("Order 10 placed: Laptop x2 for 75000"));
    }
}
