package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.external.TmdbApiClient;
import com.filmograph.auth_server.movie.service.MovieExternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자용 영화 관리 API
 * 외부 API(TMDB)에서 영화 데이터를 가져오는 기능
 */
@RestController
@RequestMapping("/api/v1/admin/movies")
@RequiredArgsConstructor
public class MovieAdminController {

    private final MovieExternalService movieExternalService;
    private final TmdbApiClient tmdbApiClient;

    /**
     * TMDB에서 영화 검색
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTmdb(@RequestParam String query,
                                        @RequestParam(defaultValue = "1") int page) {
        try {
            TmdbApiClient.TmdbSearchResponse response = tmdbApiClient.searchMovies(query, page);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "TMDB API 호출 실패", "message", e.getMessage()));
        }
    }

    /**
     * TMDB에서 영화를 검색하고 데이터베이스에 저장
     */
    @PostMapping("/import")
    public ResponseEntity<?> importMovie(@RequestParam String query) {
        try {
            Movie movie = movieExternalService.searchAndSaveMovie(query);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "영화가 성공적으로 가져와졌습니다",
                    "movieId", movie.getId(),
                    "title", movie.getTitle()
            ));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.err.println("========================================");
            System.err.println("=== TMDB 영화 가져오기 실패 (DataIntegrityViolationException) ===");
            System.err.println("Error: " + e.getMessage());
            if (e.getRootCause() != null) {
                System.err.println("Root Cause: " + e.getRootCause().getMessage());
                System.err.println("Root Cause Class: " + e.getRootCause().getClass().getName());
            }
            e.printStackTrace();
            // DataIntegrityViolationException은 전역 핸들러로 전달
            throw e;
        } catch (org.springframework.transaction.UnexpectedRollbackException e) {
            System.err.println("========================================");
            System.err.println("=== TMDB 영화 가져오기 실패 (UnexpectedRollbackException) ===");
            System.err.println("Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                System.err.println("Cause Class: " + e.getCause().getClass().getName());
                if (e.getCause().getCause() != null) {
                    System.err.println("Root Cause: " + e.getCause().getCause().getMessage());
                    System.err.println("Root Cause Class: " + e.getCause().getCause().getClass().getName());
                }
            }
            e.printStackTrace();
            // UnexpectedRollbackException은 전역 핸들러로 전달
            throw e;
        } catch (RuntimeException e) {
            System.err.println("========================================");
            System.err.println("=== TMDB 영화 가져오기 실패 (RuntimeException) ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Exception Class: " + e.getClass().getName());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                System.err.println("Cause Class: " + e.getCause().getClass().getName());
            }
            e.printStackTrace();
            // RuntimeException은 그대로 전파하여 전역 핸들러가 처리하도록 함
            throw e;
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("=== TMDB 영화 가져오기 실패 (Exception) ===");
            System.err.println("Exception Type: " + e.getClass().getName());
            System.err.println("Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            // 모든 예외를 RuntimeException으로 래핑하여 전파
            throw new RuntimeException("영화 가져오기 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"), e);
        }
    }

    /**
     * TMDB 인기 영화 목록 조회
     */
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularMovies(@RequestParam(defaultValue = "1") int page) {
        try {
            List<TmdbApiClient.TmdbMovie> movies = movieExternalService.getPopularMovies(page);
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "TMDB API 호출 실패", "message", e.getMessage()));
        }
    }

    /**
     * TMDB 인기 영화를 DB에 대량 저장
     * @param pages 저장할 페이지 수 (1페이지 = 20개 영화, 기본값: 5페이지 = 100개)
     */
    @PostMapping("/import/popular")
    public ResponseEntity<?> importPopularMovies(@RequestParam(defaultValue = "5") int pages) {
        try {
            int savedCount = movieExternalService.importPopularMovies(pages);
            // 저장된 영화 중 첫 번째와 마지막 영화 ID 반환 (디버깅용)
            List<com.filmograph.auth_server.movie.domain.Movie> allMovies = 
                    movieExternalService.getMovieRepository().findAll();
            Long firstId = allMovies.isEmpty() ? null : allMovies.get(0).getId();
            Long lastId = allMovies.isEmpty() ? null : allMovies.get(allMovies.size() - 1).getId();
            
            return ResponseEntity.ok(Map.of(
                    "message", "인기 영화 가져오기 완료",
                    "pages", pages,
                    "savedCount", savedCount,
                    "totalMoviesInDb", allMovies.size(),
                    "firstMovieId", firstId != null ? firstId : 0,
                    "lastMovieId", lastId != null ? lastId : 0
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "인기 영화 가져오기 실패", "message", e.getMessage()));
        }
    }

    /**
     * TMDB 트렌딩 영화를 DB에 대량 저장
     * @param timeWindow "day" 또는 "week" (기본값: "day")
     * @param pages 저장할 페이지 수 (기본값: 5페이지 = 100개)
     */
    @PostMapping("/import/trending")
    public ResponseEntity<?> importTrendingMovies(
            @RequestParam(defaultValue = "day") String timeWindow,
            @RequestParam(defaultValue = "5") int pages) {
        try {
            if (!timeWindow.equals("day") && !timeWindow.equals("week")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "timeWindow는 'day' 또는 'week'여야 합니다."));
            }
            int savedCount = movieExternalService.importTrendingMovies(timeWindow, pages);
            return ResponseEntity.ok(Map.of(
                    "message", "트렌딩 영화 가져오기 완료",
                    "timeWindow", timeWindow,
                    "pages", pages,
                    "savedCount", savedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "트렌딩 영화 가져오기 실패", "message", e.getMessage()));
        }
    }

    /**
     * TMDB Top Rated 영화를 DB에 대량 저장
     * @param pages 저장할 페이지 수 (기본값: 5페이지 = 100개)
     */
    @PostMapping("/import/top-rated")
    public ResponseEntity<?> importTopRatedMovies(@RequestParam(defaultValue = "5") int pages) {
        try {
            int savedCount = movieExternalService.importTopRatedMovies(pages);
            return ResponseEntity.ok(Map.of(
                    "message", "Top Rated 영화 가져오기 완료",
                    "pages", pages,
                    "savedCount", savedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Top Rated 영화 가져오기 실패", "message", e.getMessage()));
        }
    }

    /**
     * TMDB 현재 상영 중인 영화를 DB에 대량 저장
     * @param pages 저장할 페이지 수 (기본값: 5페이지 = 100개)
     */
    @PostMapping("/import/now-playing")
    public ResponseEntity<?> importNowPlayingMovies(@RequestParam(defaultValue = "5") int pages) {
        try {
            int savedCount = movieExternalService.importNowPlayingMovies(pages);
            return ResponseEntity.ok(Map.of(
                    "message", "현재 상영 중인 영화 가져오기 완료",
                    "pages", pages,
                    "savedCount", savedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "현재 상영 중인 영화 가져오기 실패", "message", e.getMessage()));
        }
    }

    /**
     * 모든 영화 삭제 (관련 데이터 포함)
     * ⚠️ 주의: 이 작업은 되돌릴 수 없습니다!
     */
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllMovies() {
        try {
            int deletedCount = movieExternalService.deleteAllMovies();
            return ResponseEntity.ok(Map.of(
                    "message", "모든 영화가 삭제되었습니다",
                    "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "영화 삭제 실패", "message", e.getMessage()));
        }
    }
}

