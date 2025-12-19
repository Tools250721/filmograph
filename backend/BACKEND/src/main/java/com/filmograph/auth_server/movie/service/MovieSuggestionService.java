package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.repo.MovieSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieSuggestionService {

    private final MovieSearchRepository repo;

    // prefix 로 제목 추천 반환 (상위 N개)
    public List<String> findSuggestions(String prefix, int limit) {
        // 첫 페이지만 필요하므로 page=0
        var pageable = PageRequest.of(0, limit);
        return repo.findSuggestions(prefix == null ? "" : prefix, pageable)
                .getContent()
                .stream()
                .map(Movie::getTitle)
                .toList();
    }
}
