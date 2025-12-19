package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.auth.SecurityUserHelper;
import com.filmograph.auth_server.movie.repo.StatsRepository;
import com.filmograph.auth_server.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    private final StatsRepository repo;
    private final SecurityUserHelper userHelper;

    public StatsController(StatsRepository repo, SecurityUserHelper userHelper) {
        this.repo = repo; 
        this.userHelper = userHelper;
    }

    @GetMapping("/genres")
    @Transactional(readOnly = true)
    public Map<String,Object> myGenres(HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            List<Object[]> rows = repo.myGenreDistribution(me.getId());
            List<String> labels = new ArrayList<>();
            List<Long> counts = new ArrayList<>();
            for (Object[] r : rows) {
                labels.add((String) r[0]);
                counts.add(((Number) r[1]).longValue());
            }
            return Map.of("labels", labels, "counts", counts);
        } catch (IllegalStateException e) {
            // 인증 오류
            throw e;
        } catch (Exception e) {
            System.err.println("장르 분포 조회 실패: " + e.getMessage());
            e.printStackTrace();
            // 에러 발생 시 빈 배열 반환
            return Map.of("labels", List.<String>of(), "counts", List.<Long>of());
        }
    }

    @GetMapping("/ratings")
    @Transactional(readOnly = true)
    public Map<String,Object> myRatings(HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            List<Object[]> rows = repo.myRatingHistogram(me.getId());
            List<Integer> bins = new ArrayList<>();
            List<Long> counts = new ArrayList<>();
            for (Object[] r : rows) {
                bins.add(((Number) r[0]).intValue());
                counts.add(((Number) r[1]).longValue());
            }
            return Map.of("bins", bins, "counts", counts);
        } catch (IllegalStateException e) {
            // 인증 오류
            throw e;
        } catch (Exception e) {
            System.err.println("평점 히스토그램 조회 실패: " + e.getMessage());
            e.printStackTrace();
            // 에러 발생 시 빈 배열 반환
            return Map.of("bins", List.<Integer>of(), "counts", List.<Long>of());
        }
    }
}
