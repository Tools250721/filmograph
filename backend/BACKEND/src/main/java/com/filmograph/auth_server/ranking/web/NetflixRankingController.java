package com.filmograph.auth_server.ranking.web;

import com.filmograph.auth_server.ranking.repo.NetflixWeeklyRankRepo;
import com.filmograph.auth_server.ranking.service.NetflixTop10Ingestor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/rankings/netflix")
public class NetflixRankingController {

    private final NetflixTop10Ingestor ingestor;
    private final NetflixWeeklyRankRepo repo;

    @PersistenceContext
    private EntityManager em;

    public NetflixRankingController(NetflixTop10Ingestor ingestor, NetflixWeeklyRankRepo repo) {
        this.ingestor = ingestor;
        this.repo = repo;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "where", "/api/v1/rankings/netflix/ping");
    }

    @PostMapping("/ingest")
    public Map<String, Object> ingestNow() {
        try {
            int n = ingestor.ingest();
            return Map.of("ok", true, "insertedOrUpdated", n);
        } catch (IOException e) {
            return Map.of("ok", false, "error", "데이터 수집 실패: " + e.getMessage());
        } catch (Exception e) {
            return Map.of("ok", false, "error", "데이터 수집 실패: " + e.getMessage(), "type", e.getClass().getSimpleName());
        }
    }

    @GetMapping("/weekly")
    public Map<String, Object> weekly(
            @RequestParam(required = false) String weekStart,
            @RequestParam String category) {

        LocalDate week = (weekStart == null) ? repo.findLatestWeek() : LocalDate.parse(weekStart);

        List<Object[]> rows = em.createQuery("""
                  SELECT n.weeklyRank, n.showTitle, n.seasonTitle, n.weeklyViews, n.weeklyHours, n.runtimeMinutes
                  FROM NetflixWeeklyRank n
                  WHERE n.weekStart = :w AND n.category = :c
                  ORDER BY n.weeklyRank ASC
                """, Object[].class)
                .setParameter("w", week)
                .setParameter("c", category)
                .getResultList();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] r : rows) {
            items.add(Map.of(
                    "rank", r[0], "title", r[1], "seasonTitle", r[2],
                    "weeklyViews", r[3], "weeklyHours", r[4], "runtimeMinutes", r[5]));
        }
        return Map.of("weekStart", week, "category", category, "items", items);
    }

    @GetMapping("/weekly/latest")
    public Map<String, Object> latestAll() {
        LocalDate latest = repo.findLatestWeek();
        List<String> cats = List.of("Films (English)", "Films (Non-English)", "TV (English)", "TV (Non-English)");
        Map<String, Object> out = new LinkedHashMap<>();
        for (String c : cats)
            out.put(c, weekly(latest.toString(), c));
        return out;
    }
}
