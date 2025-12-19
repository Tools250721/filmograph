package com.filmograph.auth_server.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 프로필 조회/업데이트 응답 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String profileImageUrl;
    private String backgroundImageUrl;
}
