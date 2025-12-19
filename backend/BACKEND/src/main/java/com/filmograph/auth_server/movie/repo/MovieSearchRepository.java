package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 영화 검색 및 자동완성 Repository
 * - 제목, 감독, 배우, 장르, 국가 등 다양한 필드 기반 검색
 * - 연도 범위 및 페이징 지원
 */
public interface MovieSearchRepository extends JpaRepository<Movie, Long> {

    /**
     * 일반 검색 (제목, 원제, 감독, 배우, 장르 등 통합 검색)
     * 띄어쓰기를 무시하여 검색 (예: "해리포터" = "해리 포터")
     * q: 검색 키워드, yearFrom~yearTo: 개봉연도 범위, pageable: 페이지네이션
     */
    @Query(value = """
        SELECT DISTINCT m.* FROM movies m
        LEFT JOIN movie_actor ma ON m.id = ma.movie_id
        LEFT JOIN actors a ON ma.actor_id = a.id
        LEFT JOIN movie_genre mg ON m.id = mg.movie_id
        LEFT JOIN genres g ON mg.genre_id = g.id
        WHERE (:q IS NULL 
               OR LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(REPLACE(m.title, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :q, '%'), ' ', ''))
               OR LOWER(m.original_title) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(REPLACE(m.original_title, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :q, '%'), ' ', ''))
               OR LOWER(m.director) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(REPLACE(m.director, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :q, '%'), ' ', ''))
               OR (a.name IS NOT NULL AND (LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(REPLACE(a.name, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :q, '%'), ' ', ''))))
               OR LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:yearFrom IS NULL OR m.release_year >= :yearFrom)
          AND (:yearTo IS NULL OR m.release_year <= :yearTo)
        ORDER BY m.release_year DESC
    """, 
    countQuery = """
        SELECT COUNT(DISTINCT m.id) FROM movies m
        LEFT JOIN movie_actor ma ON m.id = ma.movie_id
        LEFT JOIN actors a ON ma.actor_id = a.id
        LEFT JOIN movie_genre mg ON m.id = mg.movie_id
        LEFT JOIN genres g ON mg.genre_id = g.id
        WHERE (:q IS NULL 
               OR LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(REPLACE(m.title, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :q, '%'), ' ', ''))
               OR LOWER(m.original_title) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(REPLACE(m.original_title, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :q, '%'), ' ', ''))
               OR LOWER(m.director) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(REPLACE(m.director, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :q, '%'), ' ', ''))
               OR (a.name IS NOT NULL AND (LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(REPLACE(a.name, ' ', '')) LIKE LOWER(REPLACE(CONCAT('%', :q, '%'), ' ', ''))))
               OR LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:yearFrom IS NULL OR m.release_year >= :yearFrom)
          AND (:yearTo IS NULL OR m.release_year <= :yearTo)
    """,
    nativeQuery = true)
    Page<Movie> search(@Param("q") String q,
                       @Param("yearFrom") Integer yearFrom,
                       @Param("yearTo") Integer yearTo,
                       Pageable pageable);


    /**
     * 자동완성 추천 (왓챠피디아 스타일)
     */
    @Query("""
        SELECT DISTINCT m FROM Movie m
        LEFT JOIN m.actors a
        LEFT JOIN m.genres g
        WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(m.originalTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(m.director) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(m.country) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR CAST(m.releaseYear AS string) LIKE CONCAT('%', :keyword, '%')
           OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Movie> findSuggestions(@Param("keyword") String keyword, Pageable pageable);


    /**
     * 감독 검색
     */
    @Query("""
        SELECT m FROM Movie m
        WHERE LOWER(m.director) LIKE LOWER(CONCAT('%', :name, '%'))
        ORDER BY m.title ASC
    """)
    Page<Movie> findByDirector(@Param("name") String name, Pageable pageable);


    /**
     * 배우 이름으로 검색
     */
    @Query("""
        SELECT DISTINCT m FROM Movie m
        JOIN m.actors a
        WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :actorName, '%'))
        ORDER BY m.title ASC
    """)
    Page<Movie> findByActor(@Param("actorName") String actorName, Pageable pageable);
}
