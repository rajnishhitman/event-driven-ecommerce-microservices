package com.example.orderservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.exception.GlobalExceptionHandler;
import com.example.orderservice.exception.UserNotFoundException;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrderReturns201() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(new OrderResponse(1L, 1L, "Laptop", 2, new BigDecimal("75000"), "CREATED"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "productName": "Laptop",
                                  "quantity": 2,
                                  "price": 75000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Laptop"));
    }

    @Test
    void createOrderValidatesQuantity() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "productName": "Laptop",
                                  "quantity": 0,
                                  "price": 75000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createOrderReturns404WhenUserMissing() throws Exception {
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateOrderRequest(99L, "Laptop", 1, new BigDecimal("100")))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    @Test
    void getOrdersReturnsList() throws Exception {
        when(orderService.getOrders()).thenReturn(List.of(
                new OrderResponse(1L, 1L, "Laptop", 2, new BigDecimal("75000"), "CREATED")));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
