package com.filmograph.auth_server.ranking.provider.repo;

import com.filmograph.auth_server.ranking.provider.domain.ProviderTrendingSnapshot;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProviderTrendingSnapshotRepo extends JpaRepository<ProviderTrendingSnapshot, Long> {

    @Query("""
      SELECT p FROM ProviderTrendingSnapshot p
       WHERE p.provider = :provider AND p.section = :section
       ORDER BY p.capturedAt DESC
    """)
    List<ProviderTrendingSnapshot> findLatest(@Param("provider") String provider,
                                              @Param("section")  String section,
                                              PageRequest page);
}
