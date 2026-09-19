package com.example.orderservice.config;

import com.example.orderservice.event.OrderCreatedEvent;
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

@Configuration
public class KafkaProducerConfig {

    public static final String ORDER_CREATED_TOPIC = "order-created";

    @Bean
    public ProducerFactory<String, OrderCreatedEvent> orderCreatedProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> orderCreatedKafkaTemplate(
            ProducerFactory<String, OrderCreatedEvent> orderCreatedProducerFactory) {
        return new KafkaTemplate<>(orderCreatedProducerFactory);
    }

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC).partitions(3).replicas(1).build();
    }
}
