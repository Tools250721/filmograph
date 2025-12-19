package com.filmograph.auth_server.auth.dto;

public record ConfirmResetPasswordRequest(
        String token,
        String newPassword
) {}
