package com.filmograph.auth_server.movie.domain;

import com.filmograph.auth_server.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "rating_likes", uniqueConstraints = @UniqueConstraint(name = "uk_rating_like_user_rating", columnNames = {"user_id", "rating_id"}))
public class RatingLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rating_id", nullable = false)
    private Rating rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public RatingLike() {
    }

    public RatingLike(Rating rating, User user) {
        this.rating = rating;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public Rating getRating() {
        return rating;
    }

    public User getUser() {
        return user;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
