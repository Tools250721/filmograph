package com.filmograph.auth_server.ranking.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tmdb_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 지역 코드 (GLOBAL, KR, US)
    @Column(nullable = false, length = 10)
    private String region;

    // ✅ MySQL 예약어 회피 → rank_value 컬럼으로 저장
    @Column(name = "rank_value", nullable = false)
    private int rank;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    // TMDB 영화 ID (실제 영화와 연결하기 위함)
    @Column(name = "tmdb_id")
    private Long tmdbId;

    // 스냅샷 시각
    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;
}
