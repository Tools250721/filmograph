package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.domain.Quote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByMovie(Movie movie);

    @Query("""
        SELECT q FROM Quote q
        LEFT JOIN FETCH q.movie
        WHERE (:movieId IS NULL OR q.movie.id = :movieId)
          AND (:lang IS NULL OR q.lang = :lang)
        ORDER BY function('RAND')
    """)
    List<Quote> randomOne(@Param("movieId") Long movieId, @Param("lang") String lang, Pageable pageable);
}

