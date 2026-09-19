package com.example.notificationservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleUserServiceDown(
            WebClientRequestException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_GATEWAY, "Unable to reach user-service", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
