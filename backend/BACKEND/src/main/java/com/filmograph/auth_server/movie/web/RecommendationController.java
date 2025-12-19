package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.repo.RecommendationRepository;
import com.filmograph.auth_server.movie.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService service;
    private final RecommendationRepository repository;

    public RecommendationController(RecommendationService service, RecommendationRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping("/by-genre")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> byGenre(@RequestParam Long genreId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "3") long minCount) {
        long movieCount = repository.countByGenreId(genreId);

        if (movieCount == 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("movies", List.of());
            response.put("totalCount", 0);
            response.put("genreId", genreId);
            return ResponseEntity.ok(response);
        }

        List<Movie> movies = service.recommendByGenre(genreId, limit, minCount);

        // LazyInitializationException 방지를 위해 Movie를 Map으로 변환
        List<Map<String, Object>> movieList = new ArrayList<>();
        for (Movie movie : movies) {
            Map<String, Object> movieMap = new HashMap<>();
            movieMap.put("id", movie.getId());
            movieMap.put("title", movie.getTitle());
            movieMap.put("originalTitle", movie.getOriginalTitle());
            movieMap.put("overview", movie.getOverview());
            movieMap.put("releaseYear", movie.getReleaseYear());
            movieMap.put("releaseDate", movie.getReleaseDate());
            movieMap.put("runtimeMinutes", movie.getRuntimeMinutes());
            movieMap.put("country", movie.getCountry());
            movieMap.put("ageRating", movie.getAgeRating());
            movieMap.put("posterUrl", movie.getPosterUrl());
            movieMap.put("backdropUrl", movie.getBackdropUrl());
            movieMap.put("director", movie.getDirector());
            movieMap.put("averageRating", movie.getAverageRating());
            movieMap.put("tmdbId", movie.getTmdbId());

            // 장르 정보 (트랜잭션 내에서 로딩 가능)
            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                List<Map<String, Object>> genres = movie.getGenres().stream()
                        .map(genre -> {
                            Map<String, Object> genreMap = new HashMap<>();
                            genreMap.put("id", genre.getId());
                            genreMap.put("name", genre.getName());
                            return genreMap;
                        })
                        .toList();
                movieMap.put("genres", genres);
            } else {
                movieMap.put("genres", List.of());
            }

            movieList.add(movieMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("movies", movieList);
        response.put("totalCount", movieList.size());
        response.put("genreId", genreId);

        return ResponseEntity.ok(response);
    }
}
