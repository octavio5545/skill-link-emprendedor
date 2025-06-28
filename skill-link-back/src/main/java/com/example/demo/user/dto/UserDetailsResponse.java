package com.example.demo.user.dto;

import com.example.demo.common.Role;
import com.example.demo.common.UserInterest;

import java.time.OffsetDateTime;
import java.util.List;

public record UserDetailsResponse(
        Long id,
        String name,
        String secondName,
        String email,
        Role role,
        List<UserInterest> interests,
        OffsetDateTime registrationDate,
        boolean active
) {
}
