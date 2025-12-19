package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.movie.dto.MovieResponseDto;
import com.filmograph.auth_server.movie.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class ReviewController {

    private final ReviewService reviewService;

    //  내가 평가한 영화 목록 조회 API
    @GetMapping("/{userId}/rated-movies")
    public ResponseEntity<List<MovieResponseDto>> getRatedMovies(@PathVariable Long userId) {
        List<MovieResponseDto> movies = reviewService.getRatedMoviesByUser(userId);
        return ResponseEntity.ok(movies);
    }
}
