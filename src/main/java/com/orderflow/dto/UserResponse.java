package com.orderflow.dto;

import com.orderflow.entity.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Boolean active
) {
}