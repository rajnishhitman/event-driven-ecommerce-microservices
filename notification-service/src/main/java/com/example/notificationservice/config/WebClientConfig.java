package com.example.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient is a non-blocking HTTP client from Spring WebFlux.
 * One shared instance is enough for this demo; production apps often add
 * timeouts, exchange filters, and connection pooling (Reactor Netty).
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
