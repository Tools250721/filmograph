package com.filmograph.auth_server.ranking.provider.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "provider_trending_snapshot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider","section","captured_at"}))
public class ProviderTrendingSnapshot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;            // TVING | WATCHA | WATCHA_PEDIA ...
    private String section;             // 지금 뜨는 콘텐츠 | 많이 본 콘텐츠 | 핫랭킹 ...
    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProviderTrendingItem> items = new ArrayList<>();
}
