package com.filmograph.auth_server.movie.domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "movie_ott",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_movie_ott",
                columnNames = {"movie_id", "provider_id", "region"}
        )
)
public class MovieOtt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private OttProvider provider;

    @Column(nullable = false, length = 8)
    private String region;

    @Column(columnDefinition = "TEXT")
    private String linkUrl;

    public Long getId() { return id; }
    public Movie getMovie() { return movie; }
    public OttProvider getProvider() { return provider; }
    public String getRegion() { return region; }
    public String getLinkUrl() { return linkUrl; }
}

