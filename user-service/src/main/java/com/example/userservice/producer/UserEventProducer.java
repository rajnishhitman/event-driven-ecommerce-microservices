package com.example.userservice.producer;

import com.example.userservice.config.KafkaProducerConfig;
import com.example.userservice.event.UserCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer for user lifecycle events.
 * Publishing is fire-and-forget from the HTTP thread's perspective: the REST call
 * does not wait for notification-service to process the event.
 */
@Component
public class UserEventProducer {

    private static final Logger log = LoggerFactory.getLogger(UserEventProducer.class);

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, UserCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(UserCreatedEvent event) {
        kafkaTemplate.send(KafkaProducerConfig.USER_CREATED_TOPIC, String.valueOf(event.userId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserCreatedEvent for userId={}", event.userId(), ex);
                    } else {
                        log.info("Published UserCreatedEvent to topic={} partition={} offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
