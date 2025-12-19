package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Movie, Long> {

    @Query("""
        select m
        from Movie m
          join m.genres g
          left join Rating r on r.movie = m
        where g.id = :genreId
        group by m.id
        having count(r.id) >= :minCount
        order by 
          case when count(r.id) > 0 then avg(r.stars) else 0 end desc,
          count(r.id) desc,
          m.id desc
    """)
    List<Movie> topByGenre(Long genreId, long minCount, Pageable pageable);
    
    /**
     * 장르별 영화 목록 (평점 개수 제한 없음)
     * 평점이 있는 영화를 우선 정렬하고, 평점이 없는 영화도 포함
     */
    @Query("""
        select m
        from Movie m
          join m.genres g
        where g.id = :genreId
        order by 
          (select coalesce(avg(r.stars), 0) from Rating r where r.movie = m) desc,
          (select count(r.id) from Rating r where r.movie = m) desc,
          m.id desc
    """)
    List<Movie> topByGenreWithoutMinCount(Long genreId, Pageable pageable);
    
    /**
     * 장르별 영화 개수 확인 (디버깅용)
     */
    @Query("""
        select count(distinct m.id)
        from Movie m
          join m.genres g
        where g.id = :genreId
    """)
    long countByGenreId(Long genreId);
}
