package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.UserMovie;
import com.filmograph.auth_server.movie.domain.UserMovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserMovieRepository extends JpaRepository<UserMovie, Long> {

    // 특정 유저 + 영화 + 상태로 찾기
    Optional<UserMovie> findByUser_IdAndMovie_IdAndStatus(Long userId, Long movieId, UserMovieStatus status);

    // 특정 유저 + 상태로 전체 조회
    List<UserMovie> findAllByUser_IdAndStatus(Long userId, UserMovieStatus status);

    // ✅ JOIN FETCH를 사용하여 Movie와 User를 한 번에 조회 (LazyInitializationException 방지)
    @Query("SELECT um FROM UserMovie um JOIN FETCH um.movie JOIN FETCH um.user WHERE um.user.id = :userId AND um.status = :status")
    List<UserMovie> findAllByUser_IdAndStatusWithFetch(@Param("userId") Long userId, @Param("status") UserMovieStatus status);
}
