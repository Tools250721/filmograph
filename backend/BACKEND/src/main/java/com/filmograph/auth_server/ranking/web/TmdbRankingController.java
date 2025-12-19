package com.filmograph.auth_server.ranking.web;

import com.filmograph.auth_server.ranking.domain.TmdbItem;
import com.filmograph.auth_server.ranking.service.TmdbService;
import com.filmograph.auth_server.movie.repo.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ranking/tmdb")
@RequiredArgsConstructor
public class TmdbRankingController {

    private final TmdbService tmdbService;
    private final MovieRepository movieRepository;

    @PostMapping("/global/refresh")
    public Map<String, Object> refreshGlobal() {
        int saved = tmdbService.refreshGlobal();
        return Map.of("ok", saved > 0, "saved", saved, "region", "GLOBAL");
    }

    @GetMapping("/global")
    public List<Map<String, Object>> getGlobal() {
        List<TmdbItem> items = tmdbService.getLatest("GLOBAL");
        if (items.isEmpty()) {
            try {
                tmdbService.refreshGlobal();
                items = tmdbService.getLatest("GLOBAL");
            } catch (Exception e) {
                return List.of();
            }
        }
        return enrichWithMovieId(items);
    }

    @PostMapping("/kr/refresh")
    public Map<String, Object> refreshKR() {
        int saved = tmdbService.refreshKorea();
        return Map.of("ok", saved > 0, "saved", saved, "region", "KR");
    }

    @GetMapping("/kr")
    public List<Map<String, Object>> getKR() {
        List<TmdbItem> items = tmdbService.getLatest("KR");
        if (items.isEmpty()) {
            try {
                tmdbService.refreshKorea();
                items = tmdbService.getLatest("KR");
            } catch (Exception e) {
                return List.of();
            }
        }
        return enrichWithMovieId(items);
    }

    @PostMapping("/us/refresh")
    public Map<String, Object> refreshUS() {
        int saved = tmdbService.refreshUS();
        return Map.of("ok", saved > 0, "saved", saved, "region", "US");
    }

    @GetMapping("/us")
    public List<Map<String, Object>> getUS() {
        List<TmdbItem> items = tmdbService.getLatest("US");
        if (items.isEmpty()) {
            try {
                tmdbService.refreshUS();
                items = tmdbService.getLatest("US");
            } catch (Exception e) {
                return List.of();
            }
        }
        return enrichWithMovieId(items);
    }

    /**
     * TmdbItem에 실제 영화 ID를 추가하여 반환
     */
    private List<Map<String, Object>> enrichWithMovieId(List<TmdbItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("rank", item.getRank());
            map.put("title", item.getTitle());

            String posterUrl = item.getPosterUrl();
            if (posterUrl == null) {
                posterUrl = "";
            }
            map.put("posterUrl", posterUrl);

            map.put("region", item.getRegion());
            map.put("snapshotAt", item.getSnapshotAt());

            // TMDB ID로 실제 영화 ID 찾기
            Long movieId = null;
            if (item.getTmdbId() != null) {
                movieId = movieRepository.findByTmdbId(item.getTmdbId())
                        .map(movie -> movie.getId())
                        .orElse(null);
            }

            // TMDB ID로 찾지 못하면 제목으로 검색
            if (movieId == null && item.getTitle() != null) {
                movieId = movieRepository.findByTitle(item.getTitle())
                        .map(movie -> movie.getId())
                        .orElse(null);
            }

            // id는 실제 영화 ID로 설정 (없으면 null)
            map.put("id", movieId);
            map.put("movieId", movieId); // 실제 영화 ID (없으면 null)
            map.put("tmdbId", item.getTmdbId()); // TMDB ID

            return map;
        }).toList();
    }
}
