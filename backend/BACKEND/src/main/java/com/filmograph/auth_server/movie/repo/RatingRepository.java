package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.domain.Rating;
import com.filmograph.auth_server.movie.dto.MyRatedMovieDto;
import com.filmograph.auth_server.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserAndMovie(User user, Movie movie);

    // ✅ 단일 조회용 JOIN FETCH (DetailPage에서 사용)
    @Query("SELECT r FROM Rating r JOIN FETCH r.user JOIN FETCH r.movie WHERE r.user.id = :userId AND r.movie.id = :movieId")
    Optional<Rating> findByUserIdAndMovieIdWithFetch(@Param("userId") Long userId, @Param("movieId") Long movieId);

    // ✅ DB 레벨 페이징 + JOIN FETCH (countQuery 필수)
    @Query(value = "SELECT r FROM Rating r JOIN FETCH r.user JOIN FETCH r.movie WHERE r.movie = :movie", countQuery = "SELECT count(r) FROM Rating r WHERE r.movie = :movie")
    Page<Rating> findByMovieWithFetch(@Param("movie") Movie movie, Pageable pageable);

    @Query(value = "SELECT r FROM Rating r JOIN FETCH r.user JOIN FETCH r.movie WHERE r.user.id = :userId", countQuery = "SELECT count(r) FROM Rating r WHERE r.user.id = :userId")
    Page<Rating> findByUserIdWithFetch(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT r FROM Rating r JOIN FETCH r.user JOIN FETCH r.movie", countQuery = "SELECT count(r) FROM Rating r")
    Page<Rating> findRecentRatingsWithFetch(Pageable pageable);

    // --- 마이페이지용 DTO 직접 조회 메서드 (기존 유지) ---
    @Query("""
                SELECT new com.filmograph.auth_server.movie.dto.MyRatedMovieDto(
                    m.id, m.title, m.posterUrl, r.stars, r.createdAt,
                    CASE WHEN um.id IS NOT NULL THEN true ELSE false END, um.createdAt
                )
                FROM Rating r
                JOIN r.movie m
                LEFT JOIN UserMovie um ON um.user.id = r.user.id AND um.movie.id = r.movie.id AND um.status = 'FAVORITE'
                WHERE r.user.id = :userId
                ORDER BY r.stars DESC, r.createdAt DESC
            """)
    Page<MyRatedMovieDto> findMyMoviesOrderByRatingDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("""
                SELECT new com.filmograph.auth_server.movie.dto.MyRatedMovieDto(
                    m.id, m.title, m.posterUrl, r.stars, r.createdAt,
                    CASE WHEN um.id IS NOT NULL THEN true ELSE false END, um.createdAt
                )
                FROM Rating r
                JOIN r.movie m
                LEFT JOIN UserMovie um ON um.user.id = r.user.id AND um.movie.id = r.movie.id AND um.status = 'FAVORITE'
                WHERE r.user.id = :userId
                ORDER BY r.stars ASC, r.createdAt DESC
            """)
    Page<MyRatedMovieDto> findMyMoviesOrderByRatingAsc(@Param("userId") Long userId, Pageable pageable);

    @Query("""
                SELECT new com.filmograph.auth_server.movie.dto.MyRatedMovieDto(
                    m.id, m.title, m.posterUrl, r.stars, r.createdAt,
                    CASE WHEN um.id IS NOT NULL THEN true ELSE false END, um.createdAt
                )
                FROM Rating r
                JOIN r.movie m
                LEFT JOIN UserMovie um ON um.user.id = r.user.id AND um.movie.id = r.movie.id AND um.status = 'FAVORITE'
                WHERE r.user.id = :userId
                ORDER BY r.createdAt DESC
            """)
    Page<MyRatedMovieDto> findMyMoviesOrderByRatedAt(@Param("userId") Long userId, Pageable pageable);

    @Query("""
                SELECT new com.filmograph.auth_server.movie.dto.MyRatedMovieDto(
                    m.id, m.title, m.posterUrl, r.stars, r.createdAt,
                    CASE WHEN um.id IS NOT NULL THEN true ELSE false END, um.createdAt
                )
                FROM Rating r
                JOIN r.movie m
                LEFT JOIN UserMovie um ON um.user.id = r.user.id AND um.movie.id = r.movie.id AND um.status = 'FAVORITE'
                WHERE r.user.id = :userId
                ORDER BY CASE WHEN um.id IS NULL THEN 1 ELSE 0 END, r.createdAt DESC
            """)
    Page<MyRatedMovieDto> findMyMoviesLikedFirst(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.movie.id = :movieId")
    Optional<Double> findAverageRatingByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.movie.id = :movieId")
    Long countByMovieId(@Param("movieId") Long movieId);
}