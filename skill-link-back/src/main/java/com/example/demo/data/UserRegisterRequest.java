package com.example.demo.data;

public record UserRegisterRequest(
        String name,
        String secondName,
        String email,
        String password,
        Role role
) {
}
