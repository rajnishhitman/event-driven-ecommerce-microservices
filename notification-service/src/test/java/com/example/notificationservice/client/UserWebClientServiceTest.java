package com.example.notificationservice.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.notificationservice.dto.UserResponse;
import com.example.notificationservice.exception.UserNotFoundException;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class UserWebClientServiceTest {

    private MockWebServer mockWebServer;
    private UserWebClientService userWebClientService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        userWebClientService = new UserWebClientService(WebClient.builder().build(), mockWebServer.url("/").toString().replaceAll("/$", ""));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getUserDeserializesJson() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":1,\"name\":\"Rajnish\",\"email\":\"rajnish@example.com\"}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(userWebClientService.getUser(1L))
                .assertNext((UserResponse user) -> {
                    assertThat(user.id()).isEqualTo(1L);
                    assertThat(user.name()).isEqualTo("Rajnish");
                })
                .verifyComplete();
    }

    @Test
    void getUserMaps404() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        StepVerifier.create(userWebClientService.getUser(1L))
                .expectError(UserNotFoundException.class)
                .verify();
    }
}
