package com.example.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.event.UserCreatedEvent;
import com.example.userservice.exception.DuplicateEmailException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.producer.UserEventProducer;
import com.example.userservice.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer userEventProducer;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userEventProducer);
    }

    @Test
    void createUserPersistsAndPublishesEvent() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Rajnish", "rajnish@example.com");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            setId(user, 1L);
            return user;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("rajnish@example.com");

        ArgumentCaptor<UserCreatedEvent> captor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(userEventProducer).publish(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(1L);
        assertThat(captor.getValue().name()).isEqualTo("Rajnish");
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        CreateUserRequest request = new CreateUserRequest("Rajnish", "rajnish@example.com");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(userRepository, never()).save(any());
        verify(userEventProducer, never()).publish(any());
    }

    @Test
    void getUserThrowsWhenMissing() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(10L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("10");
    }

    @Test
    void getUsersMapsEntities() throws Exception {
        User user = new User("Rajnish", "rajnish@example.com");
        setId(user, 1L);
        when(userRepository.findAll()).thenReturn(List.of(user));

        assertThat(userService.getUsers()).containsExactly(
                new UserResponse(1L, "Rajnish", "rajnish@example.com"));
    }

    private static void setId(User user, Long id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}
