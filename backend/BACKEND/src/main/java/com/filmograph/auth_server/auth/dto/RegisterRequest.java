package com.filmograph.auth_server.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.filmograph.auth_server.user.User;         // ← User 엔티티 경로 맞춰주세요
import jakarta.validation.constraints.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 100)
        String name,

        @NotBlank @Email @Size(max = 190)
        String email,

        @NotBlank @Size(min = 8, max = 64,
                message = "비밀번호는 8~64자여야 합니다.")
        String password
) {
    /*이메일을 소문자/트림해서 사용하고 싶을 때 */
    public String normalizedEmail() {
        return email == null ? null : email.trim().toLowerCase();
    }

    /** 서비스에서 인코딩된 비번을 받아 User 엔티티로 변환 */
    public User toEntity(String encodedPassword) {
        return User.builder()
                .name(name)
                .email(normalizedEmail())
                .passwordHash(encodedPassword) // ★ DB 컬럼 password_hash 로 매핑됨
                .build();
    }

    /* 로그에 비밀번호가 찍히지 않도록 마스킹 */
    @Override
    public String toString() {
        return "RegisterRequest[name=%s, email=%s, password=***]".formatted(name, normalizedEmail());
    }
}
