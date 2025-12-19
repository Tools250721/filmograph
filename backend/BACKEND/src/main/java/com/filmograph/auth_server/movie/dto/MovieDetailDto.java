package com.filmograph.auth_server.movie.dto;

import java.util.List;

/**
 *  영화 상세 정보 DTO
 */
public record MovieDetailDto(
        Long id,
        String title,
        String originalTitle,
        String overview,
        Integer releaseYear,
        Integer runtimeMinutes,
        String country,
        String ageRating,
        String posterUrl,
        String backdropUrl,
        List<NamedDto> genres,      // 장르
        List<PersonDto> directors,  // 감독
        List<ActorDto> actors,      // 반드시 ActorDto
        List<OttDto> ott,           // OTT
        StatsDto stats,             // 평점 정보
        List<ImageDto> stills       // 스틸컷 이미지 리스트
) {}
