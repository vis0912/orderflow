package com.orderflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException exception) {

        Map<String, Object> response = new HashMap<>();

        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("message", exception.getMessage());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}