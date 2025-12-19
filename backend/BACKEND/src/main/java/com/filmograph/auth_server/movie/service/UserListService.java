package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.domain.UserMovie;
import com.filmograph.auth_server.movie.domain.UserMovieStatus;
import com.filmograph.auth_server.movie.dto.MovieResponseDto;
import com.filmograph.auth_server.movie.dto.UserMovieResponseDto;
import com.filmograph.auth_server.movie.repo.MovieRepository;
import com.filmograph.auth_server.movie.repo.UserMovieRepository;
import com.filmograph.auth_server.user.User;
import com.filmograph.auth_server.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserListService {

    private final UserMovieRepository repo;
    private final MovieRepository movieRepo;
    private final UserRepository userRepo;

    public UserListService(UserMovieRepository repo, MovieRepository movieRepo, UserRepository userRepo) {
        this.repo = repo;
        this.movieRepo = movieRepo;
        this.userRepo = userRepo;
    }

    /** ✅ 유저의 영화 목록에 추가 */
    @Transactional
    public void add(Long userId, Long movieId, UserMovieStatus status) {
        try {
            Movie movie = movieRepo.findById(movieId)
                    .orElseThrow(() -> new IllegalArgumentException("movie not found: " + movieId));

            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));

            Optional<UserMovie> existing = repo.findByUser_IdAndMovie_IdAndStatus(userId, movieId, status);
            if (existing.isPresent()) {
                return;
            }

            UserMovie um = new UserMovie();
            um.setUser(user);
            um.setMovie(movie);
            um.setStatus(status);

            repo.save(um);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            String rootCauseMsg = e.getRootCause() != null ? e.getRootCause().getMessage() : "";
            if (rootCauseMsg.contains("duplicate") || rootCauseMsg.contains("UNIQUE")) {
                return;
            }
            if (rootCauseMsg.contains("Data truncated") || rootCauseMsg.contains("truncated")) {
                throw new RuntimeException("데이터베이스 컬럼 길이 부족: status 컬럼을 확인해주세요. (현재 status: " + status.name() + ")", e);
            }
            throw new RuntimeException("데이터베이스 제약 조건 위반: " + rootCauseMsg, e);
        } catch (Exception e) {
            throw new RuntimeException("영화 목록 추가 실패: " + e.getMessage(), e);
        }
    }

    /** 유저의 영화 목록에서 제거 */
    @Transactional
    public void remove(Long userId, Long movieId, UserMovieStatus status) {
        repo.findByUser_IdAndMovie_IdAndStatus(userId, movieId, status)
                .ifPresent(repo::delete);
    }

    /** 유저의 영화 목록 조회 */
    @Transactional(readOnly = true)
    public List<UserMovieResponseDto> listDto(Long userId, UserMovieStatus status) {
        // JOIN FETCH를 사용하여 Movie와 User를 한 번에 조회 (LazyInitializationException 방지)
        List<UserMovie> userMovies = repo.findAllByUser_IdAndStatusWithFetch(userId, status);

        // 트랜잭션 내에서 모든 필요한 데이터 로드
        return userMovies.stream()
                .map(um -> {
                    Movie movie = um.getMovie();
                    // 트랜잭션 내에서 지연 로딩된 컬렉션 접근하여 초기화
                    if (movie.getActors() != null) {
                        movie.getActors().size(); // 컬렉션 초기화
                    }
                    if (movie.getGenres() != null) {
                        movie.getGenres().size(); // 컬렉션 초기화
                    }
                    return new UserMovieResponseDto(movie, um.getCreatedAt());
                })
                .toList();
    }
}
