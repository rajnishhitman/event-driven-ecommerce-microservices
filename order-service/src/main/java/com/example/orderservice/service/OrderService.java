package com.example.orderservice.service;

import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.exception.UserNotFoundException;
import com.example.orderservice.exception.UserServiceUnavailableException;
import com.example.orderservice.producer.OrderEventProducer;
import com.example.orderservice.repository.OrderRepository;
import feign.FeignException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    public static final String STATUS_CREATED = "CREATED";

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final OrderEventProducer orderEventProducer;

    public OrderService(
            OrderRepository orderRepository,
            UserClient userClient,
            OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        verifyUserExists(request.userId());

        Order saved = orderRepository.save(new Order(
                request.userId(),
                request.productName(),
                request.quantity(),
                request.price(),
                STATUS_CREATED
        ));

        orderEventProducer.publish(new OrderCreatedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getProductName(),
                saved.getQuantity(),
                saved.getPrice()
        ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private void verifyUserExists(Long userId) {
        try {
            userClient.getUserById(userId);
        } catch (FeignException.NotFound ex) {
            throw new UserNotFoundException(userId);
        } catch (FeignException ex) {
            throw new UserServiceUnavailableException(
                    "Unable to verify user with user-service", ex);
        }
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus()
        );
    }
}
