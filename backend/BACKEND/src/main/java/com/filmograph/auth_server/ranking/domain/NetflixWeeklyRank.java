package com.filmograph.auth_server.ranking.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "netflix_weekly_rank",
        uniqueConstraints = @UniqueConstraint(columnNames = {"week_start","category","weekly_rank"})
)
public class NetflixWeeklyRank {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="week_start", nullable=false)
    private LocalDate weekStart;

    @Column(nullable=false, length=40)
    private String category; // Films (English) ...

    @Column(name="weekly_rank", nullable=false)
    private Integer weeklyRank;

    @Column(name="show_title", columnDefinition="TEXT", nullable=false)
    private String showTitle;

    @Column(name="season_title")
    private String seasonTitle;

    @Column(name="weekly_views")
    private Long weeklyViews;

    @Column(name="weekly_hours")
    private Long weeklyHours;

    @Column(name="runtime_minutes")
    private Integer runtimeMinutes;

    // --- 기본 생성자/게터세터 ---
    public NetflixWeeklyRank() {}
    // getters & setters 생략하면 롬복 써도 됨(@Getter/@Setter)
}
