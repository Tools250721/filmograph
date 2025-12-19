package com.filmograph.auth_server.movie.dto;

import java.time.LocalDateTime;

public class MyRatedMovieDto {

    private Long movieId;
    private String title;
    private String posterUrl;
    private Double stars;
    private LocalDateTime ratedAt;
    private Boolean isFavorite;
    private LocalDateTime favoriteAt;

    // 이 생성자가 정확히 있어야 함
    public MyRatedMovieDto(Long movieId,
            String title,
            String posterUrl,
            Double stars,
            LocalDateTime ratedAt,
            Boolean isFavorite,
            LocalDateTime favoriteAt) {
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.stars = stars;
        this.ratedAt = ratedAt;
        this.isFavorite = isFavorite;
        this.favoriteAt = favoriteAt;
    }

    public Long getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public Double getStars() {
        return stars;
    }

    public LocalDateTime getRatedAt() {
        return ratedAt;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public LocalDateTime getFavoriteAt() {
        return favoriteAt;
    }
}
