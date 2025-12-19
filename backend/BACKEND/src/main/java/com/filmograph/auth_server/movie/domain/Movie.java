package com.filmograph.auth_server.movie.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 기본 정보
    @Column(length = 500)
    private String title;           // 영화 제목
    
    @Column(length = 500)
    private String originalTitle;   // 원제
    
    @Column(columnDefinition = "TEXT")
    private String overview;        // 줄거리 (매우 긴 텍스트 가능)
    
    @Column(length = 20)
    private String releaseDate;     // 개봉일 (YYYY-MM-DD)
    
    private Integer releaseYear;    // 개봉연도
    private Integer runtimeMinutes; // 상영시간(분)
    
    @Column(length = 10)
    private String country;         // 제작 국가
    
    @Column(length = 20)
    private String ageRating;       // 관람 등급
    
    @Column(length = 1000)
    private String posterUrl;       // 포스터 이미지 URL
    
    @Column(length = 1000)
    private String backdropUrl;     // 배경 이미지 URL
    
    @Column(length = 200)
    private String director;        // 감독 이름
    
    private Double averageRating;   // 평균 평점
    private Long tmdbId;            // TMDB 영화 ID (외부 API 연동용)

    // ✅ 연관 관계
    @ManyToMany
    @JoinTable(
            name = "movie_actor",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    @Builder.Default
    private List<Actor> actors = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "id")
    )
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();
}
