package com.filmograph.auth_server.ranking.repo;

import com.filmograph.auth_server.ranking.domain.NetflixWeeklyRank;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface NetflixWeeklyRankRepo extends JpaRepository<NetflixWeeklyRank, Long> {

    @Modifying @Transactional
    @Query(value = """
    INSERT INTO netflix_weekly_rank
      (week_start, category, weekly_rank, show_title, season_title, weekly_views, weekly_hours, runtime_minutes)
    VALUES
      (:weekStart, :category, :rank, :title, :season, :views, :hours, :runtime)
    ON DUPLICATE KEY UPDATE
      show_title = VALUES(show_title),
      season_title = VALUES(season_title),
      weekly_views = COALESCE(VALUES(weekly_views), weekly_views),
      weekly_hours = COALESCE(VALUES(weekly_hours), weekly_hours),
      runtime_minutes = COALESCE(VALUES(runtime_minutes), runtime_minutes)
  """, nativeQuery = true)
    void upsert(
            @Param("weekStart") LocalDate weekStart,
            @Param("category") String category,
            @Param("rank") int rank,
            @Param("title") String title,
            @Param("season") String season,
            @Param("views") Long views,
            @Param("hours") Long hours,
            @Param("runtime") Integer runtime
    );

    @Query("SELECT MAX(n.weekStart) FROM NetflixWeeklyRank n")
    LocalDate findLatestWeek();
}
