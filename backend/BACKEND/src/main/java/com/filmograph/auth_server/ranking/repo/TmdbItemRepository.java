package com.filmograph.auth_server.ranking.repo;

import com.filmograph.auth_server.ranking.domain.TmdbItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TmdbItemRepository extends JpaRepository<TmdbItem, Long> {

    @Query("SELECT t FROM TmdbItem t " +
            "WHERE t.region = :region " +
            "AND t.snapshotAt = (SELECT MAX(t2.snapshotAt) FROM TmdbItem t2 WHERE t2.region = :region) " +
            "ORDER BY t.rank ASC")
    List<TmdbItem> findLatestByRegion(@Param("region") String region);
}
