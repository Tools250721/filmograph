package com.filmograph.auth_server.movie.dto;

/**
 * OTT 제공처 정보를 전달하기 위한 DTO
 */
public record OttDto(
        Long id,          // OTT Provider ID
        String name,      // 제공처 이름 (Netflix, Watcha 등)
        String type,      // 구독/대여/구매 (SUBSCRIPTION, RENT, BUY)
        String region,    // KR / US / JP 등
        String logoUrl,   // 로고 이미지 URL
        String linkUrl    // 시청 링크
) {}
