package com.example.userservice.producer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.userservice.config.KafkaProducerConfig;
import com.example.userservice.event.UserCreatedEvent;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class UserEventProducerTest {

    @Mock
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @Test
    void publishSendsJsonEventWithUserIdKey() {
        UserEventProducer producer = new UserEventProducer(kafkaTemplate);
        UserCreatedEvent event = new UserCreatedEvent(1L, "Rajnish", "rajnish@example.com");
        when(kafkaTemplate.send(eq(KafkaProducerConfig.USER_CREATED_TOPIC), eq("1"), eq(event)))
                .thenReturn(new CompletableFuture<SendResult<String, UserCreatedEvent>>());

        producer.publish(event);

        verify(kafkaTemplate).send(KafkaProducerConfig.USER_CREATED_TOPIC, "1", event);
    }
}
