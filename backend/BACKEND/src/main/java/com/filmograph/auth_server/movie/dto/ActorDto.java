package com.filmograph.auth_server.movie.dto;

/**
 * 영화 배우 정보 DTO
 * - TMDb 검색 결과에서 profileUrl 자동 매핑됨
 */
public record ActorDto(
        Long id,
        String name,
        String character,
        Integer order,
        String profileUrl
) {}
