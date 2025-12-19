package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.domain.Rating;
import com.filmograph.auth_server.movie.domain.RatingLike;
import com.filmograph.auth_server.movie.dto.*;
import com.filmograph.auth_server.movie.repo.RatingRepository;
import com.filmograph.auth_server.movie.repo.RatingLikeRepository;
import com.filmograph.auth_server.movie.repo.MovieRepository;
import com.filmograph.auth_server.user.User;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Optional;

@Service
public class RatingService {

    private final RatingRepository ratingRepo;
    private final RatingLikeRepository ratingLikeRepo;
    private final MovieRepository movieRepo;

    public RatingService(RatingRepository ratingRepo, RatingLikeRepository ratingLikeRepo, MovieRepository movieRepo) {
        this.ratingRepo = ratingRepo;
        this.ratingLikeRepo = ratingLikeRepo;
        this.movieRepo = movieRepo;
    }

    @Transactional
    public RatingDto createOrUpdate(User user, Long movieId, Double stars, String review, Boolean spoiler) {
        Movie movie = movieRepo.findById(movieId).orElseThrow(() -> new RuntimeException("movie not found"));
        // JOIN FETCH를 사용하여 준영속 상태 문제 방지
        Rating rating = ratingRepo.findByUserIdAndMovieIdWithFetch(user.getId(), movieId)
                .orElse(new Rating(user, movie, stars, review, spoiler));

        rating.setStars(stars != null ? stars : rating.getStars());
        rating.setReview(review);
        rating.setSpoiler(spoiler != null ? spoiler : false);
        if (rating.getLikeCount() == null) {
            rating.setLikeCount(0);
        }
        Rating saved = ratingRepo.save(rating);

        String userName = null;
        if (saved.getUser() != null) {
            Hibernate.initialize(saved.getUser());
            userName = saved.getUser().getName();
        }

        RatingDto dto = new RatingDto(
                saved.getId(),
                movieId,
                saved.getStars(),
                saved.getReview(),
                saved.getSpoiler(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                userName);
        dto.likeCount = saved.getLikeCount() != null ? saved.getLikeCount() : 0;
        dto.isLiked = ratingLikeRepo.existsByRatingAndUser(saved, user);
        return dto;
    }

    @Transactional
    public void delete(User user, Long movieId) {
        // JOIN FETCH를 사용하여 준영속 상태 문제 방지
        ratingRepo.findByUserIdAndMovieIdWithFetch(user.getId(), movieId)
                .ifPresent(rating -> {
                    // RatingLike를 먼저 삭제하여 외래 키 제약 조건 위반 방지
                    List<RatingLike> likes = ratingLikeRepo.findByRating(rating);
                    if (!likes.isEmpty()) {
                        ratingLikeRepo.deleteAll(likes);
                    }
                    ratingRepo.delete(rating);
                });
    }

    @Transactional(readOnly = true)
    public Page<RatingDto> listByMovie(Long movieId, int page, int size, Optional<User> currentUser) {
        Movie movie = movieRepo.findById(movieId).orElseThrow(() -> new RuntimeException("movie not found"));

        // DB 레벨에서 필요한 페이지만큼 JOIN FETCH로 가져옴
        Page<Rating> p = ratingRepo.findByMovieWithFetch(movie,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));

        return p.map(r -> {
            String userName = null;
            if (r.getUser() != null) {
                Hibernate.initialize(r.getUser());
                userName = r.getUser().getName();
            }

            RatingDto dto = new RatingDto(
                    r.getId(),
                    movieId,
                    r.getStars(),
                    r.getReview(),
                    r.getSpoiler(),
                    r.getCreatedAt(),
                    r.getUpdatedAt(),
                    userName);
            dto.likeCount = r.getLikeCount() != null ? r.getLikeCount() : 0;
            dto.isLiked = currentUser.isPresent() && ratingLikeRepo.existsByRatingAndUser(r, currentUser.get());
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<RatingDto> listByUser(User user, int page, int size) {
        // ID를 사용하여 준영속 상태 문제 방지 및 DB 페이징 수행
        Page<Rating> p = ratingRepo.findByUserIdWithFetch(user.getId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));

        return p.map(r -> {
            String userName = null;
            if (r.getUser() != null) {
                Hibernate.initialize(r.getUser());
                userName = r.getUser().getName();
            }

            RatingDto dto = new RatingDto(
                    r.getId(),
                    r.getMovie().getId(),
                    r.getStars(),
                    r.getReview(),
                    r.getSpoiler(),
                    r.getCreatedAt(),
                    r.getUpdatedAt(),
                    userName);
            dto.likeCount = r.getLikeCount() != null ? r.getLikeCount() : 0;
            dto.isLiked = ratingLikeRepo.existsByRatingAndUser(r, user);
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<RatingDto> listRecent(int page, int size, Optional<User> currentUser) {
        // 전체 최근 평점 DB 페이징 조회
        Page<Rating> p = ratingRepo.findRecentRatingsWithFetch(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        // 트랜잭션 내에서 User를 명시적으로 초기화하여 LazyInitializationException 방지
        return p.map(r -> {
            String userName = null;
            if (r.getUser() != null) {
                // User 프록시를 명시적으로 초기화
                Hibernate.initialize(r.getUser());
                userName = r.getUser().getName();
            }

            RatingDto dto = new RatingDto(
                    r.getId(),
                    r.getMovie().getId(),
                    r.getStars(),
                    r.getReview(),
                    r.getSpoiler(),
                    r.getCreatedAt(),
                    r.getUpdatedAt(),
                    userName);
            dto.likeCount = r.getLikeCount() != null ? r.getLikeCount() : 0;
            dto.isLiked = currentUser.isPresent() && ratingLikeRepo.existsByRatingAndUser(r, currentUser.get());
            return dto;
        });
    }

    // ✅ DetailPage에서 사용: 특정 영화에 대한 내 평점 조회
    @Transactional(readOnly = true)
    public RatingDto getMyRatingForMovie(User user, Long movieId) {
        // JOIN FETCH를 사용하여 User와 Movie를 한 번에 조회 (준영속 상태 문제 해결)
        return ratingRepo.findByUserIdAndMovieIdWithFetch(user.getId(), movieId)
                .map(r -> {
                    String userName = null;
                    if (r.getUser() != null) {
                        Hibernate.initialize(r.getUser());
                        userName = r.getUser().getName();
                    }

                    RatingDto dto = new RatingDto(
                            r.getId(),
                            movieId,
                            r.getStars(),
                            r.getReview(),
                            r.getSpoiler(),
                            r.getCreatedAt(),
                            r.getUpdatedAt(),
                            userName);
                    dto.likeCount = r.getLikeCount() != null ? r.getLikeCount() : 0;
                    dto.isLiked = ratingLikeRepo.existsByRatingAndUser(r, user);
                    return dto;
                })
                .orElse(null); // 평점이 없으면 null 반환
    }

    @Transactional
    public void toggleLike(User user, Long ratingId) {
        Rating rating = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));

        var existingLike = ratingLikeRepo.findByRatingAndUser(rating, user);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            ratingLikeRepo.delete(existingLike.get());
            rating.setLikeCount(Math.max(0, rating.getLikeCount() - 1));
        } else {
            // 좋아요 추가
            RatingLike like = new RatingLike(rating, user);
            ratingLikeRepo.save(like);
            rating.setLikeCount(rating.getLikeCount() + 1);
        }

        ratingRepo.save(rating);
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(User user, Long ratingId) {
        Rating rating = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        return ratingLikeRepo.existsByRatingAndUser(rating, user);
    }
}