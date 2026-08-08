package com.orderflow.dto;

public record LoginResponse(
        String token,
        String tokenType
) {
}