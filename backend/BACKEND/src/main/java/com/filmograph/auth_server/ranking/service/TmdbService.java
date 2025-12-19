package com.filmograph.auth_server.ranking.service;

import com.filmograph.auth_server.ranking.domain.TmdbItem;
import com.filmograph.auth_server.ranking.repo.TmdbItemRepository;
import com.filmograph.auth_server.ranking.provider.service.TmdbFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbService {

    private final TmdbItemRepository repo;
    private final TmdbFetcher fetcher;

    @Transactional
    public int refreshGlobal() {
        return saveItems(fetcher.fetchTrendingGlobal(), "GLOBAL");
    }

    @Transactional
    public int refreshKorea() {
        return saveItems(fetcher.fetchPopularKR(), "KR");
    }

    @Transactional
    public int refreshUS() {
        return saveItems(fetcher.fetchPopularUS(), "US");
    }

    private int saveItems(List<Map<String, Object>> src, String region) {
        if (src.isEmpty()) {
            log.warn("No TMDb items fetched for region={}", region);
            return 0;
        }

        Instant snap = Instant.now();
        List<TmdbItem> batch = new ArrayList<>();

        int rank = 1;
        for (Map<String, Object> m : src) {
            String title = String.valueOf(m.get("title"));
            if (title == null || title.isBlank()) {
                title = "Untitled"; // 안전장치
            }

            String posterPath = String.valueOf(m.get("poster_path"));
            String posterUrl = (posterPath == null || posterPath.equals("null") || posterPath.isBlank())
                    ? ""
                    : "https://image.tmdb.org/t/p/w500" + posterPath;

            // TMDB ID 추출
            Long tmdbId = null;
            Object idObj = m.get("id");
            if (idObj != null) {
                if (idObj instanceof Number) {
                    tmdbId = ((Number) idObj).longValue();
                } else if (idObj instanceof String) {
                    try {
                        tmdbId = Long.parseLong((String) idObj);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid TMDB ID format: {}", idObj);
                    }
                }
            }

            batch.add(TmdbItem.builder()
                    .rank(rank++)
                    .title(title)
                    .posterUrl(posterUrl)
                    .tmdbId(tmdbId)
                    .region(region)
                    .snapshotAt(snap)
                    .build());

            if (rank > 20)
                break; // Top 20만 저장
        }

        repo.saveAll(batch);
        log.info("✅ Saved {} TMDb items for region={} (snapshotAt={})", batch.size(), region, snap);
        return batch.size();
    }

    @Transactional(readOnly = true)
    public List<TmdbItem> getLatest(String region) {
        return repo.findLatestByRegion(region);
    }
}
