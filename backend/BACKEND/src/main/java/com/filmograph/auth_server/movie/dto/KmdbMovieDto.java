package com.filmograph.auth_server.movie.dto;

import lombok.Data;
import java.util.List;

@Data
public class KmdbMovieDto {
    private List<DataSection> Data;

    @Data
    public static class DataSection {
        private List<Result> Result;
    }

    @Data
    public static class Result {
        private String movieId;
        private String title;
        private String titleEng;
        private String prodYear;
        private String posters;
        private Plots plots;
    }

    @Data
    public static class Plots {
        private List<Plot> plot;
    }

    @Data
    public static class Plot {
        private String plotText;
    }
}
