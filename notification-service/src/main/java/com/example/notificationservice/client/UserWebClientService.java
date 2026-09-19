package com.example.notificationservice.client;

import com.example.notificationservice.dto.UserResponse;
import com.example.notificationservice.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Outbound HTTP to user-service using WebClient (non-blocking, reactive).
 * Returns a Mono so callers can compose the response without blocking the event loop
 * until Spring MVC subscribes (MVC supports Mono return types).
 */
@Service
public class UserWebClientService {

    private final WebClient webClient;
    private final String userServiceUrl;

    public UserWebClientService(WebClient webClient, @Value("${user-service.url}") String userServiceUrl) {
        this.webClient = webClient;
        this.userServiceUrl = userServiceUrl;
    }

    public Mono<UserResponse> getUser(Long userId) {
        return webClient.get()
                .uri(userServiceUrl + "/users/{id}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().value() == 404) {
                        return Mono.error(new UserNotFoundException(userId));
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(UserResponse.class);
    }
}
