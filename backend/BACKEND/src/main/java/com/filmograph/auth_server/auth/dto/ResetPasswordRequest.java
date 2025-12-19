package com.filmograph.auth_server.auth.dto;

//public record ResetPasswordRequest(String token, String newPassword,String email) {}

public class ResetPasswordRequest {
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
