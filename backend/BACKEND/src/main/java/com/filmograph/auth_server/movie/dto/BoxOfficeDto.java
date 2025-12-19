package com.filmograph.auth_server.movie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 박스오피스 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxOfficeDto {
    private int rank; // 순위
    private String movieNm; // 영화명
    private String openDt; // 개봉일
    private String salesAcc; // 누적 매출액
    private String audiAcc; // 누적 관객수
    private Long movieId; // 로컬 DB 영화 ID (있으면)
    private String posterUrl; // 포스터 URL (있으면)
}
