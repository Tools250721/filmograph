package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Movie;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Movie, Long> {

    // 내 장르 분포: rating 기준(내가 평가한 영화들)
    @Query("""
        select g.name as genre, count(distinct m.id) as cnt
        from Rating r
          join r.movie m
          join m.genres g
        where r.user.id = :userId
        group by g.name
        order by cnt desc
    """)
    List<Object[]> myGenreDistribution(Long userId);

    // 내 평점 분포(1~5)
    @Query("""
        select r.stars as stars, count(r.id) as cnt
        from Rating r
        where r.user.id = :userId
        group by r.stars
        order by r.stars
    """)
    List<Object[]> myRatingHistogram(Long userId);
}
