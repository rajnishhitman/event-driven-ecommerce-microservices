package com.example.orderservice.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.orderservice.dto.UserResponse;
import org.junit.jupiter.api.Test;

class UserClientTest {

    @Test
    void feignClientContractReturnsUserResponse() {
        UserClient userClient = mock(UserClient.class);
        when(userClient.getUserById(1L)).thenReturn(new UserResponse(1L, "Rajnish", "rajnish@example.com"));

        UserResponse user = userClient.getUserById(1L);

        assertThat(user.id()).isEqualTo(1L);
        assertThat(user.email()).isEqualTo("rajnish@example.com");
    }
}
