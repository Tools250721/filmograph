package com.filmograph.auth_server.movie.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ott_provider")
public class OttProvider {

    public enum Type { SUBSCRIPTION, RENT, BUY }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Type type;

    @Column(columnDefinition = "TEXT")
    private String logoUrl;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Type getType() { return type; }
    public String getLogoUrl() { return logoUrl; }
}

