package com.filmograph.auth_server.ranking.provider.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "provider_trending_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"snapshot_id","rank"}))
public class ProviderTrendingItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "snapshot_id")
    private ProviderTrendingSnapshot snapshot;

    @Column(name = "ranking")
    private Integer ranking;               // 1..N
    @Column(columnDefinition = "TEXT")
    private String title;
    private Integer year;               // 있으면
    @Column(name = "content_type")
    private String contentType;         // movie | tv (구분 가능한 경우)
    @Column(name = "ext_id")
    private String extId;
    @Column(name = "poster_url", columnDefinition = "TEXT")
    private String posterUrl;
}
