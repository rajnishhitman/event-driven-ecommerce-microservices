package com.example.orderservice.producer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.orderservice.config.KafkaProducerConfig;
import com.example.orderservice.event.OrderCreatedEvent;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Test
    void publishUsesUserIdAsPartitionKey() {
        OrderEventProducer producer = new OrderEventProducer(kafkaTemplate);
        OrderCreatedEvent event = new OrderCreatedEvent(10L, 1L, "Laptop", 2, new BigDecimal("75000"));
        when(kafkaTemplate.send(eq(KafkaProducerConfig.ORDER_CREATED_TOPIC), eq("1"), eq(event)))
                .thenReturn(new CompletableFuture<SendResult<String, OrderCreatedEvent>>());

        producer.publish(event);

        verify(kafkaTemplate).send(KafkaProducerConfig.ORDER_CREATED_TOPIC, "1", event);
    }
}
