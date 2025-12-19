package com.filmograph.auth_server.movie.dto;

/*감독 / 일반 인물 정보 DTO, TMDb에서 이미지 자동 매칭됨
 */
public record PersonDto(
        Long id,
        String name,
        String profileUrl
) {}
