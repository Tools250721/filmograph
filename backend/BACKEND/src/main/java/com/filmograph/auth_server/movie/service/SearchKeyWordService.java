package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.SearchKeyword;
import com.filmograph.auth_server.movie.repo.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchKeyWordService {

    private final SearchKeywordRepository searchKeywordRepository;

    // 인기 검색어 10개 가져오기
    public List<String> getPopularKeywords() {
        return searchKeywordRepository.findTop10ByOrderBySearchCountDesc()
                .stream()
                .map(SearchKeyword::getKeyword)
                .toList();
    }

    // 검색 실행 시 카운트 기록
    @Transactional
    public void recordSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return;

        SearchKeyword searchKeyword = searchKeywordRepository.findByKeywordIgnoreCase(keyword)
                .orElse(SearchKeyword.builder()
                        .keyword(keyword)
                        .searchCount(0L)
                        .lastSearched(LocalDateTime.now())
                        .build());

        searchKeyword.setSearchCount(searchKeyword.getSearchCount() + 1);
        searchKeyword.setLastSearched(LocalDateTime.now());

        searchKeywordRepository.save(searchKeyword);
    }
}
