package com.filmograph.auth_server.movie.dto;

/**
 * 이미지 정보 DTO
 */
public record ImageDto(
        String url,        // 이미지 URL
        Double aspectRatio, // 종횡비
        Integer width,     // 너비
        Integer height     // 높이
) {}

