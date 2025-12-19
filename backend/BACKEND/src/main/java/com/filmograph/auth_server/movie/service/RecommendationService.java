package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.repo.RecommendationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    private final RecommendationRepository repo;

    public RecommendationService(RecommendationRepository repo) { this.repo = repo; }

    public List<Movie> recommendByGenre(Long genreId, int limit, long minCount) {
        // minCount가 0이면 평점 개수 제한 없이 모든 장르별 영화 반환
        if (minCount == 0) {
            return repo.topByGenreWithoutMinCount(genreId, PageRequest.of(0, limit));
        }
        return repo.topByGenre(genreId, minCount, PageRequest.of(0, limit));
    }
}
