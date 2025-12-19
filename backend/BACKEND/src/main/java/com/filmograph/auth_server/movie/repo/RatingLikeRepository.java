package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Rating;
import com.filmograph.auth_server.movie.domain.RatingLike;
import com.filmograph.auth_server.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingLikeRepository extends JpaRepository<RatingLike, Long> {
    Optional<RatingLike> findByRatingAndUser(Rating rating, User user);

    boolean existsByRatingAndUser(Rating rating, User user);

    long countByRating(Rating rating);

    List<RatingLike> findByRating(Rating rating);
}
