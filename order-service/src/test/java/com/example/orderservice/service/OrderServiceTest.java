package com.example.orderservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.UserResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.exception.UserNotFoundException;
import com.example.orderservice.exception.UserServiceUnavailableException;
import com.example.orderservice.producer.OrderEventProducer;
import com.example.orderservice.repository.OrderRepository;
import feign.FeignException;
import feign.Request;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private OrderEventProducer orderEventProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, userClient, orderEventProducer);
    }

    @Test
    void createOrderVerifiesUserThenPublishes() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Laptop", 2, new BigDecimal("75000"));
        when(userClient.getUserById(1L)).thenReturn(new UserResponse(1L, "Rajnish", "rajnish@example.com"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            setId(order, 10L);
            return order;
        });

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo("CREATED");
        verify(userClient).getUserById(1L);

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(orderEventProducer).publish(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(10L);
        assertThat(captor.getValue().productName()).isEqualTo("Laptop");
    }

    @Test
    void createOrderFailsWhenUserMissing() {
        CreateOrderRequest request = new CreateOrderRequest(99L, "Laptop", 1, new BigDecimal("100"));
        Request feignRequest = Request.create(Request.HttpMethod.GET, "/users/99", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        when(userClient.getUserById(99L)).thenThrow(new FeignException.NotFound("not found", feignRequest, null, null));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(UserNotFoundException.class);
        verify(orderRepository, never()).save(any());
        verify(orderEventProducer, never()).publish(any());
    }

    @Test
    void createOrderMapsFeignOutage() {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Laptop", 1, new BigDecimal("100"));
        Request feignRequest = Request.create(Request.HttpMethod.GET, "/users/1", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        when(userClient.getUserById(1L)).thenThrow(new FeignException.ServiceUnavailable("down", feignRequest, null, null));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(UserServiceUnavailableException.class);
    }

    @Test
    void getOrderThrowsWhenMissing() {
        when(orderRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getOrder(5L)).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void getOrdersReturnsMappedList() throws Exception {
        Order order = new Order(1L, "Laptop", 2, new BigDecimal("75000"), "CREATED");
        setId(order, 10L);
        when(orderRepository.findAll()).thenReturn(List.of(order));

        assertThat(orderService.getOrders()).hasSize(1);
        assertThat(orderService.getOrders().get(0).id()).isEqualTo(10L);
    }

    private static void setId(Order order, Long id) throws Exception {
        Field field = Order.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(order, id);
    }
}
