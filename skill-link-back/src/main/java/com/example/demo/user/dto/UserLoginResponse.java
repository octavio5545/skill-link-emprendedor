package com.example.demo.user.dto;

import com.example.demo.common.Role;
import com.example.demo.common.UserInterest;

import java.util.List;

public record UserLoginResponse(
        String token,
        UserResponse user
) {
}