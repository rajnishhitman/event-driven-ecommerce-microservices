package com.example.orderservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUserServiceDown(
            UserServiceUnavailableException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message, request);
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
