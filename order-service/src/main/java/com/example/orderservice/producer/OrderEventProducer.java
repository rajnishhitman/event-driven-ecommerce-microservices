package com.example.orderservice.producer;

import com.example.orderservice.config.KafkaProducerConfig;
import com.example.orderservice.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OrderCreatedEvent event) {
        kafkaTemplate.send(KafkaProducerConfig.ORDER_CREATED_TOPIC, String.valueOf(event.userId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderCreatedEvent for orderId={}", event.orderId(), ex);
                    } else {
                        log.info("Published OrderCreatedEvent to topic={} partition={} offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
