package com.filmograph.auth_server.movie.dto;

import java.time.LocalDateTime;

public class RatingDto {
    public Long id;
    public Long movieId;
    public double stars;
    public String review;
    public Boolean spoiler;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public String userName; // 사용자 이름 (선택적)
    public Integer likeCount = 0; // 좋아요 수
    public Boolean isLiked = false; // 현재 사용자가 좋아요를 눌렀는지 여부

    public RatingDto(Long id, Long movieId, double stars, String review, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.movieId = movieId;
        this.stars = stars;
        this.review = review;
        this.spoiler = false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userName = null;
    }

    public RatingDto(Long id, Long movieId, double stars, String review, LocalDateTime createdAt,
            LocalDateTime updatedAt, String userName) {
        this.id = id;
        this.movieId = movieId;
        this.stars = stars;
        this.review = review;
        this.spoiler = false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userName = userName;
    }

    public RatingDto(Long id, Long movieId, double stars, String review, Boolean spoiler, LocalDateTime createdAt,
            LocalDateTime updatedAt, String userName) {
        this.id = id;
        this.movieId = movieId;
        this.stars = stars;
        this.review = review;
        this.spoiler = spoiler != null ? spoiler : false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userName = userName;
    }
}
