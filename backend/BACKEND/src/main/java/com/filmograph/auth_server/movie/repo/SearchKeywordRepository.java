package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {
    Optional<SearchKeyword> findByKeywordIgnoreCase(String keyword);
    List<SearchKeyword> findTop10ByOrderBySearchCountDesc();
}
