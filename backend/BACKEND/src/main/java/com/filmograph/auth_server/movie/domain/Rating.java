package com.filmograph.auth_server.movie.domain;

import com.filmograph.auth_server.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rating", uniqueConstraints = @UniqueConstraint(name = "uk_rating_user_movie", columnNames = { "user_id",
        "movie_id" }))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false)
    private Double stars; // 1.0~5.0

    @Column(columnDefinition = "TEXT")
    private String review;

    @Column(nullable = false)
    private Boolean spoiler = false;

    @Column(nullable = false)
    private Integer likeCount = 0;

    // ⬇⬇ LocalDateTime 으로 변경
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Rating() {
    }

    public Rating(User user, Movie movie, Double stars, String review) {
        this.user = user;
        this.movie = movie;
        this.stars = stars;
        this.review = review;
        this.spoiler = false;
    }

    public Rating(User user, Movie movie, Double stars, String review, Boolean spoiler) {
        this.user = user;
        this.movie = movie;
        this.stars = stars;
        this.review = review;
        this.spoiler = spoiler != null ? spoiler : false;
    }

    @PreUpdate
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Movie getMovie() {
        return movie;
    }

    public Double getStars() {
        return stars;
    }

    public String getReview() {
        return review;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStars(Double s) {
        this.stars = s;
    }

    public void setReview(String r) {
        this.review = r;
    }

    public Boolean getSpoiler() {
        return spoiler;
    }

    public void setSpoiler(Boolean spoiler) {
        this.spoiler = spoiler != null ? spoiler : false;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount != null ? likeCount : 0;
    }
}
