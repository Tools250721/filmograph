package com.filmograph.auth_server.movie.dto;

import lombok.Data;
import java.util.List;

@Data
public class KobisMovieDto {
    private MovieListResult movieListResult;

    @Data
    public static class MovieListResult {
        private List<Movie> movieList;
    }

    @Data
    public static class Movie {
        private String movieCd;   // 영화 코드
        private String movieNm;   // 영화명
        private String prdtYear;  // 제작년도
        private String nationAlt; // 제작국가
        private String showTm;    // 상영시간
    }
}
