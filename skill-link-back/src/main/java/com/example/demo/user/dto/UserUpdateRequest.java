package com.example.demo.user.dto;

import com.example.demo.common.UserInterest;
import jakarta.validation.constraints.Email;

import java.util.List;

public record UserUpdateRequest(
        String name,
        String secondName,
        @Email(message = "El formato del correo electrónico no es válido")
        String email,
        List<UserInterest> interests
) {
}
