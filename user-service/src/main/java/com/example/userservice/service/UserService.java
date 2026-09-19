package com.example.userservice.service;

import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.event.UserCreatedEvent;
import com.example.userservice.exception.DuplicateEmailException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.producer.UserEventProducer;
import com.example.userservice.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    public UserService(UserRepository userRepository, UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User saved = userRepository.save(new User(request.name(), request.email()));

        // Persist first, then publish. If Kafka is down, the user still exists;
        // a production system would outbox/retry this event.
        userEventProducer.publish(new UserCreatedEvent(saved.getId(), saved.getName(), saved.getEmail()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
