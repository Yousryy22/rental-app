package com.rentalapp.auth.dto;

import com.rentalapp.user.Role;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String fullName,
        Role role,
        String accessToken,
        String refreshToken
) {
}
