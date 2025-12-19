package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.dto.MovieDetailDto;
import com.filmograph.auth_server.movie.dto.MyRatedMovieDto;
import com.filmograph.auth_server.movie.dto.OttDto;
import com.filmograph.auth_server.movie.service.MovieQueryService;
import com.filmograph.auth_server.movie.service.MovieService;
import com.filmograph.auth_server.movie.service.OttService;
import com.filmograph.auth_server.auth.SecurityUserHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 영화 관련 API 컨트롤러
 * - 영화 상세 조회
 * - 영화 목록 조회
 * - 영화 검색
 * - 내가 평가한 영화
 * - OTT 제공처 정보
 */
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

        private final MovieService movieService;
        private final MovieQueryService movieQueryService;
        private final OttService ottService;
        private final SecurityUserHelper securityUserHelper;
        private final com.filmograph.auth_server.movie.repo.MovieRepository movieRepository;

        /**
         * 영화 상세 정보 조회
         */
        @GetMapping("/{id}")
        public MovieDetailDto getMovieDetail(@PathVariable Long id) {
                return movieService.getDetail(id);
        }

        /**
         * 영화 목록 조회 (페이징)
         */
        @GetMapping("/list")
        public ResponseEntity<Map<String, Object>> listMovies(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                                size);
                org.springframework.data.domain.Page<Movie> movies = movieRepository.findAll(pageable);

                List<Map<String, Object>> movieList = movies.getContent().stream()
                                .map(m -> {
                                        Map<String, Object> movieMap = new HashMap<>();
                                        movieMap.put("id", m.getId());
                                        movieMap.put("title", m.getTitle() != null ? m.getTitle() : "");
                                        movieMap.put("tmdbId", m.getTmdbId() != null ? m.getTmdbId() : 0);
                                        return movieMap;
                                })
                                .toList();

                Map<String, Object> response = new HashMap<>();
                response.put("content", movieList);
                response.put("totalElements", movies.getTotalElements());
                response.put("totalPages", movies.getTotalPages());
                response.put("number", page);
                response.put("size", size);
                response.put("first", page == 0);
                response.put("last", page >= movies.getTotalPages() - 1);

                return ResponseEntity.ok(response);
        }

        /**
         * 영화 검색 (로컬 DB + TMDB)
         */
        @GetMapping("/search")
        public ResponseEntity<Map<String, Object>> searchMovies(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) Long genreId,
                        @RequestParam(required = false) Integer yearFrom,
                        @RequestParam(required = false) Integer yearTo,
                        @RequestParam(defaultValue = "id") String sort,
                        @RequestParam(defaultValue = "desc") String order,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {
                // 검색어가 없으면 기본 정렬을 연도 내림차순으로 변경 (최신 영화 우선)
                final String actualSort = ((q == null || q.trim().isEmpty()) && "id".equals(sort)) ? "year" : sort;
                final String actualOrder = ((q == null || q.trim().isEmpty()) && "id".equals(sort)) ? "desc" : order;

                Page<Movie> moviePage = movieQueryService.search(q, genreId, yearFrom, yearTo, actualSort, actualOrder,
                                page, size);

                // Movie 엔티티를 Map으로 변환 (지연 로딩 문제 방지)
                Map<String, Movie> uniqueMovies = new HashMap<>();
                for (Movie movie : moviePage.getContent()) {
                        String key = (movie.getTitle() != null ? movie.getTitle() : "") + "_" +
                                        (movie.getReleaseYear() != null ? movie.getReleaseYear() : "");
                        if (!uniqueMovies.containsKey(key) ||
                                        (uniqueMovies.get(key).getId() > movie.getId())) {
                                uniqueMovies.put(key, movie);
                        }
                }

                final String sortKey = actualSort;
                final String orderKey = actualOrder;
                List<Map<String, Object>> content = uniqueMovies.values().stream()
                                .sorted((m1, m2) -> {
                                        if ("year".equals(sortKey) && "desc".equals(orderKey)) {
                                                int yearCompare = Integer.compare(
                                                                m2.getReleaseYear() != null ? m2.getReleaseYear() : 0,
                                                                m1.getReleaseYear() != null ? m1.getReleaseYear() : 0);
                                                if (yearCompare != 0)
                                                        return yearCompare;
                                        }
                                        return Long.compare(m1.getId(), m2.getId());
                                })
                                .map(movie -> {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("id", movie.getId());
                                        map.put("title", movie.getTitle());
                                        map.put("originalTitle", movie.getOriginalTitle());
                                        map.put("overview", movie.getOverview());
                                        map.put("releaseYear", movie.getReleaseYear());
                                        map.put("runtimeMinutes", movie.getRuntimeMinutes());
                                        map.put("country", movie.getCountry());
                                        map.put("ageRating", movie.getAgeRating());
                                        map.put("posterUrl", movie.getPosterUrl());
                                        map.put("backdropUrl", movie.getBackdropUrl());
                                        map.put("director", movie.getDirector());
                                        map.put("averageRating", movie.getAverageRating());
                                        map.put("tmdbId", movie.getTmdbId());

                                        // 장르 정보 추가
                                        try {
                                                if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                                                        List<Map<String, Object>> genres = movie.getGenres().stream()
                                                                        .map(genre -> {
                                                                                Map<String, Object> genreMap = new HashMap<>();
                                                                                genreMap.put("id", genre.getId());
                                                                                genreMap.put("name", genre.getName());
                                                                                return genreMap;
                                                                        })
                                                                        .toList();
                                                        map.put("genres", genres);
                                                } else {
                                                        map.put("genres", List.of());
                                                }
                                        } catch (Exception e) {
                                                map.put("genres", List.of());
                                        }

                                        return map;
                                })
                                .toList();

                int uniqueCount = content.size();
                Map<String, Object> response = new HashMap<>();
                response.put("content", content);
                response.put("totalElements", uniqueCount);
                response.put("totalPages", uniqueCount > 0 ? 1 : 0);
                response.put("number", moviePage.getNumber());
                response.put("size", size);
                response.put("first", true);
                response.put("last", true);

                return ResponseEntity.ok(response);
        }

        /**
         * 내가 평가한 영화 목록
         */
        @GetMapping("/me")
        public Page<MyRatedMovieDto> getMyRatedMovies(
                        HttpServletRequest request,
                        @RequestParam(defaultValue = "rated_desc") String sort,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                Long userId = securityUserHelper.getCurrentUserId(request);
                return movieQueryService.getMyRatedMovies(userId, sort, page, size);
        }

        /**
         * OTT 제공처 조회
         */
        @GetMapping("/{id}/availability")
        public List<OttDto> getAvailability(
                        @PathVariable Long id,
                        @RequestParam(required = false) String region) {
                return ottService.getAvailability(id, region);
        }

        /**
         * JustWatch에서 OTT 정보 가져오기
         */
        @PostMapping("/{id}/availability/fetch")
        public ResponseEntity<List<OttDto>> fetchAvailability(
                        @PathVariable Long id,
                        @RequestParam String title,
                        @RequestParam(defaultValue = "ko_KR") String region) {
                List<OttDto> list = ottService.fetchAndSaveFromJustWatch(id, title, region);
                return ResponseEntity.ok(list);
        }
}
