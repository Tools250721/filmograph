package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.auth.SecurityUserHelper;
import com.filmograph.auth_server.movie.domain.UserMovieStatus;
import com.filmograph.auth_server.movie.dto.MovieResponseDto;
import com.filmograph.auth_server.movie.dto.RedirectResponse;
import com.filmograph.auth_server.movie.dto.UserMovieResponseDto;
import com.filmograph.auth_server.movie.service.UserListService;
import com.filmograph.auth_server.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserListController {

    private final UserListService service;
    private final SecurityUserHelper userHelper;

    @Value("${filmograph.redirect.wishlist:http://localhost:5173/wishlist}")
    private String wishlistUrl;

    @Value("${filmograph.redirect.watching:http://localhost:5173/watching}")
    private String watchingUrl;

    @Value("${filmograph.redirect.favorites:}")
    private String favoritesUrl;

    public UserListController(UserListService service, SecurityUserHelper userHelper) {
        this.service = service;
        this.userHelper = userHelper;
    }

    // ================== BUCKET (위시리스트) ==================
    @PostMapping("/bucket/{movieId}")
    @Transactional
    public ResponseEntity<?> addBucket(@PathVariable Long movieId, HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            service.add(me.getId(), movieId, UserMovieStatus.WISHLIST);
            return ResponseEntity.ok(new RedirectResponse(true, "bucket", movieId, wishlistUrl));

        } catch (IllegalStateException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", e.getMessage() != null ? e.getMessage() : "위시리스트 추가 실패"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false,
                            "error", e.getMessage() != null ? e.getMessage() : "위시리스트 추가 실패",
                            "type", e.getClass().getSimpleName()));
        }
    }

    @DeleteMapping("/bucket/{movieId}")
    @Transactional
    public ResponseEntity<?> removeBucket(@PathVariable Long movieId, HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            service.remove(me.getId(), movieId, UserMovieStatus.WISHLIST);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // 인증 오류
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", "위시리스트 삭제 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/bucket")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getBucket(HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            List<UserMovieResponseDto> movies = service.listDto(me.getId(), UserMovieStatus.WISHLIST);
            return ResponseEntity.ok(movies);
        } catch (IllegalStateException e) {
            // 인증 오류 - 로그인하지 않은 사용자는 빈 배열 반환
            String message = e.getMessage();
            if (message != null
                    && (message.contains("토큰") || message.contains("Authorization") || message.contains("헤더"))) {
                return ResponseEntity.ok(List.of());
            }
            // 기타 인증 오류는 그대로 throw
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", "위시리스트 조회 실패: " + e.getMessage()));
        }
    }

    // ================== FAVORITE (즐겨찾기) ==================
    @PostMapping("/favorites/{movieId}")
    public ResponseEntity<?> addFavorite(@PathVariable Long movieId, HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            service.add(me.getId(), movieId, UserMovieStatus.FAVORITE);
            String redirect = (favoritesUrl != null && !favoritesUrl.isBlank()) ? favoritesUrl : wishlistUrl;
            return ResponseEntity.ok(new RedirectResponse(true, "favorites", movieId, redirect));

        } catch (IllegalStateException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "error", e.getMessage()));
        } catch (org.springframework.transaction.UnexpectedRollbackException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false,
                            "error", "데이터 저장 중 오류가 발생했습니다. 입력값을 확인해주세요.",
                            "hint", "중복된 데이터이거나 필수 필드가 누락되었을 수 있습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", e.getMessage() != null ? e.getMessage() : "즐겨찾기 추가 실패"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false,
                            "error", e.getMessage() != null ? e.getMessage() : "즐겨찾기 추가 실패",
                            "type", e.getClass().getSimpleName()));
        }
    }

    @DeleteMapping("/favorites/{movieId}")
    @Transactional
    public ResponseEntity<?> removeFavorite(@PathVariable Long movieId, HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            service.remove(me.getId(), movieId, UserMovieStatus.FAVORITE);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", "즐겨찾기 삭제 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/favorites")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getFavorites(HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            List<UserMovieResponseDto> movies = service.listDto(me.getId(), UserMovieStatus.FAVORITE);
            return ResponseEntity.ok(movies);
        } catch (IllegalStateException e) {
            // 인증 오류 - 로그인하지 않은 사용자는 빈 배열 반환
            String message = e.getMessage();
            if (message != null
                    && (message.contains("토큰") || message.contains("Authorization") || message.contains("헤더"))) {
                return ResponseEntity.ok(List.of());
            }
            // 기타 인증 오류는 그대로 throw
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", "즐겨찾기 조회 실패: " + e.getMessage()));
        }
    }

    // ================== WATCHED (시청한 영화) ==================
    @PostMapping("/watched/{movieId}")
    public ResponseEntity<?> addWatched(@PathVariable Long movieId, HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            service.add(me.getId(), movieId, UserMovieStatus.WATCHED);
            return ResponseEntity.ok(new RedirectResponse(true, "watched", movieId, watchingUrl));

        } catch (IllegalStateException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "error", e.getMessage()));
        } catch (org.springframework.transaction.UnexpectedRollbackException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false,
                            "error", "데이터 저장 중 오류가 발생했습니다. 입력값을 확인해주세요.",
                            "hint", "중복된 데이터이거나 필수 필드가 누락되었을 수 있습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", e.getMessage() != null ? e.getMessage() : "시청한 영화 추가 실패"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false,
                            "error", e.getMessage() != null ? e.getMessage() : "시청한 영화 추가 실패",
                            "type", e.getClass().getSimpleName()));
        }
    }

    @DeleteMapping("/watched/{movieId}")
    @Transactional
    public ResponseEntity<?> removeWatched(@PathVariable Long movieId, HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            service.remove(me.getId(), movieId, UserMovieStatus.WATCHED);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", "시청한 영화 삭제 실패: " + e.getMessage()));
        }
    }

    @GetMapping("/watched")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getWatched(HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            List<UserMovieResponseDto> movies = service.listDto(me.getId(), UserMovieStatus.WATCHED);
            return ResponseEntity.ok(movies);
        } catch (IllegalStateException e) {
            // 인증 오류 - 로그인하지 않은 사용자는 빈 배열 반환
            String message = e.getMessage();
            if (message != null
                    && (message.contains("토큰") || message.contains("Authorization") || message.contains("헤더"))) {
                return ResponseEntity.ok(List.of());
            }
            // 기타 인증 오류는 그대로 throw
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", "시청한 영화 조회 실패: " + e.getMessage()));
        }
    }
}
