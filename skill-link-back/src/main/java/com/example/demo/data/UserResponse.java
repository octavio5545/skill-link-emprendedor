package com.example.demo.data;

public record UserResponse(
        Long id,
        String name,
        String secondName,
        String email,
        Role role
) {
}
