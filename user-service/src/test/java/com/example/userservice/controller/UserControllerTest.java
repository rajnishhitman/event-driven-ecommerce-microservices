package com.example.userservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.exception.GlobalExceptionHandler;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void createUserReturns201() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(new UserResponse(1L, "Rajnish", "rajnish@example.com"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("Rajnish", "rajnish@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("rajnish@example.com"));
    }

    @Test
    void createUserValidatesBody() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/users"));
    }

    @Test
    void getUserReturns404WhenMissing() throws Exception {
        when(userService.getUser(10L)).thenThrow(new UserNotFoundException(10L));

        mockMvc.perform(get("/users/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 10"))
                .andExpect(jsonPath("$.path").value("/users/10"));
    }

    @Test
    void getUsersReturnsList() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(new UserResponse(1L, "Rajnish", "rajnish@example.com")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rajnish"));
    }
}
