package com.filmograph.auth_server.auth.dto;

public record RegisterResponse(
        Long id,
        String name,
        String email
) {}
