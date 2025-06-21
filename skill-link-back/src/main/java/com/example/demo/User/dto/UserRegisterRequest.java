package com.example.demo.User.dto;

import com.example.demo.common.Role;

public record UserRegisterRequest(
        String name,
        String secondName,
        String email,
        String password,
        Role role
) {
}
