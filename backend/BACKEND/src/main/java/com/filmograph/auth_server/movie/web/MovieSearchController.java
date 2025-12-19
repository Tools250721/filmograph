package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.movie.dto.MovieResponseDto;
import com.filmograph.auth_server.movie.service.KmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * KMDB 외부 API 검색 컨트롤러
 * 한국영화데이터베이스(KMDB)에서 영화를 검색하는 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieSearchController {

    private final KmdbService kmdbService;

    /**
     * KMDB에서 영화 검색 (제목, 배우명, 연도 모두 검색 가능)
     */
    @GetMapping("/search")
    public ResponseEntity<List<MovieResponseDto>> searchMovies(
            @RequestParam String keyword,
            @RequestParam(required = false) String year) {

        List<MovieResponseDto> results = kmdbService.searchMovies(keyword, year);
        return ResponseEntity.ok(results);
    }
}
