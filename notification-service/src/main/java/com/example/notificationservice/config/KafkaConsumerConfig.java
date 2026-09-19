package com.example.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Consumer JSON settings live in application.yml.
 * Per-listener spring.json.value.default.type maps each topic to its event class.
 *
 * @EnableKafka is already pulled in by Spring Boot auto-configuration when
 * spring-kafka is on the classpath; it is declared here to make the intent obvious.
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {
}
