package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.auth.SecurityUserHelper;
import com.filmograph.auth_server.movie.dto.*;
import com.filmograph.auth_server.movie.service.RatingService;
import com.filmograph.auth_server.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RatingController {

    private final RatingService ratingService;
    private final SecurityUserHelper userHelper;

    public RatingController(RatingService ratingService, SecurityUserHelper userHelper) {
        this.ratingService = ratingService;
        this.userHelper = userHelper;
    }

    // 내 별점/리뷰 생성 또는 수정
    @PostMapping("/movies/{movieId}/ratings")
    public ResponseEntity<RatingDto> upsert(
            @PathVariable Long movieId,
            @RequestBody RatingCreateRequest body,
            HttpServletRequest req) {
        User me = userHelper.requireCurrentUser(req);
        RatingDto dto = ratingService.createOrUpdate(me, movieId, body.stars, body.review, body.spoiler);
        return ResponseEntity.ok(dto);
    }

    // 내 별점/리뷰 삭제
    @DeleteMapping("/movies/{movieId}/ratings")
    public ResponseEntity<Void> delete(@PathVariable Long movieId, HttpServletRequest req) {
        User me = userHelper.requireCurrentUser(req);
        ratingService.delete(me, movieId);
        return ResponseEntity.noContent().build();
    }

    // 특정 영화에 대한 내 평점 조회 (DetailPage용) - 더 구체적인 경로를 먼저 매핑
    @GetMapping("/movies/{movieId}/ratings/me")
    public ResponseEntity<RatingDto> getMyRatingForMovie(
            @PathVariable Long movieId,
            HttpServletRequest req) {
        User me = userHelper.requireCurrentUser(req);
        RatingDto dto = ratingService.getMyRatingForMovie(me, movieId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    // 영화 별점 리스트(전체)
    @GetMapping("/movies/{movieId}/ratings")
    public Page<RatingDto> listByMovie(@PathVariable Long movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {
        java.util.Optional<User> currentUser = userHelper.getCurrentUser(req);
        return ratingService.listByMovie(movieId, page, size, currentUser);
    }

    // 내가 쓴 별점/리뷰 목록
    @GetMapping("/users/me/ratings")
    @Transactional(readOnly = true)
    public Page<RatingDto> myRatings(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {
        User me = userHelper.requireCurrentUser(req);
        return ratingService.listByUser(me, page, size);
    }

    // 전체 최근 평점 조회 (모든 영화)
    @GetMapping("/ratings/recent")
    public Page<RatingDto> recentRatings(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest req) {
        java.util.Optional<User> currentUser = userHelper.getCurrentUser(req);
        return ratingService.listRecent(page, size, currentUser);
    }

    // 평점 좋아요 토글
    @PostMapping("/ratings/{ratingId}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long ratingId,
            HttpServletRequest req) {
        User me = userHelper.requireCurrentUser(req);
        ratingService.toggleLike(me, ratingId);
        return ResponseEntity.ok("좋아요 상태 변경 완료!");
    }
}
