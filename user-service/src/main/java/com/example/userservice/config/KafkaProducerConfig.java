package com.example.userservice.config;

import com.example.userservice.event.UserCreatedEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Explicit producer setup so the JSON contract is obvious:
 * String key (userId) + JSON value (UserCreatedEvent) without Java type headers.
 */
@Configuration
public class KafkaProducerConfig {

    public static final String USER_CREATED_TOPIC = "user-created";

    @Bean
    public ProducerFactory<String, UserCreatedEvent> userCreatedProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, UserCreatedEvent> userCreatedKafkaTemplate(
            ProducerFactory<String, UserCreatedEvent> userCreatedProducerFactory) {
        return new KafkaTemplate<>(userCreatedProducerFactory);
    }

    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name(USER_CREATED_TOPIC).partitions(3).replicas(1).build();
    }
}
