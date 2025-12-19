package com.filmograph.auth_server.auth.dto;

public record LoginResponse(
        Long id,
        String name,
        String email,
        String accessToken,
        String refreshToken
) {}
