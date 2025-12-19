package com.filmograph.auth_server.movie.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingRequest {
    private Long movieId;   // 평가할 영화 ID
    private Integer stars;  // 별점 (1~5)
    private String review;  // 리뷰 (한줄평)
}
