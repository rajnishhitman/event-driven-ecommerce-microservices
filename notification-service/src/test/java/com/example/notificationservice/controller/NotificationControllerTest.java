package com.example.notificationservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notificationservice.dto.NotificationResponse;
import com.example.notificationservice.dto.UserNotificationResponse;
import com.example.notificationservice.dto.UserResponse;
import com.example.notificationservice.exception.GlobalExceptionHandler;
import com.example.notificationservice.exception.UserNotFoundException;
import com.example.notificationservice.service.NotificationService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getUserNotificationsReturnsCombinedPayload() throws Exception {
        UserNotificationResponse payload = new UserNotificationResponse(
                new UserResponse(1L, "Rajnish", "rajnish@example.com"),
                List.of(new NotificationResponse(
                        5L, 1L, "USER_CREATED", "Welcome Rajnish", Instant.parse("2026-01-01T00:00:00Z")))
        );
        when(notificationService.getNotificationsForUser(1L)).thenReturn(Mono.just(payload));

        MvcResult result = mockMvc.perform(get("/notifications/user/1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("Rajnish"))
                .andExpect(jsonPath("$.notifications[0].type").value("USER_CREATED"));
    }

    @Test
    void getUserNotificationsReturns404() throws Exception {
        when(notificationService.getNotificationsForUser(10L)).thenReturn(Mono.error(new UserNotFoundException(10L)));

        MvcResult result = mockMvc.perform(get("/notifications/user/10"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 10"));
    }
}
