package com.example.orderservice.client;

import com.example.orderservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign is used here instead of WebClient because order creation is a
 * request/response business step: we must know whether the user exists before
 * writing the order. A declarative blocking client keeps that orchestration
 * simple and readable.
 *
 * Feign generates a JDK proxy that maps this interface to HTTP calls
 * (typically via Apache HttpClient / Java HttpClient under the hood).
 *
 * WebClient would be a better fit if this call were part of a reactive chain
 * or if we needed streaming / non-blocking IO at high concurrency.
 */
@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
