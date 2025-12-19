package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.movie.dto.BoxOfficeDto;
import com.filmograph.auth_server.movie.service.BoxOfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 박스오피스 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
public class BoxOfficeController {

    private final BoxOfficeService boxOfficeService;

    /**
     * 일일 박스오피스 Top10 조회
     */
    @GetMapping("/boxoffice/daily")
    public ResponseEntity<List<BoxOfficeDto>> getDailyBoxOfficeTop10() {
        return ResponseEntity.ok(boxOfficeService.getDailyBoxOfficeTop10());
    }
}
