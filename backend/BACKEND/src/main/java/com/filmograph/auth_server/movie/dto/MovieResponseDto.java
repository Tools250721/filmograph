package com.filmograph.auth_server.movie.dto;

import com.filmograph.auth_server.movie.domain.Actor;
import com.filmograph.auth_server.movie.domain.Genre;
import com.filmograph.auth_server.movie.domain.Movie;
import lombok.*;

import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDto {

    private Long id;
    private String title;
    private String releaseDate;
    private String nation;
    private String director;
    private String plot;
    private String posterUrl;
    private String actors;
    private String genre;
    private String rating;
    private String runtime;

    /** ✅ Movie 엔티티 → DTO 변환 생성자 */
    public MovieResponseDto(Movie movie) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.releaseDate = movie.getReleaseDate();
        this.nation = movie.getCountry(); // ✅ 필드명 일치
        this.director = movie.getDirector();
        this.plot = movie.getOverview(); // ✅ plot → overview
        this.posterUrl = movie.getPosterUrl();

        // ✅ List<Actor> → 문자열 변환
        this.actors = movie.getActors() != null && !movie.getActors().isEmpty()
                ? movie.getActors().stream()
                .map(Actor::getName) // Actor 엔티티에 getName() 메서드 있다고 가정
                .collect(Collectors.joining(", "))
                : null;

        // ✅ List<Genre> → 문자열 변환
        this.genre = movie.getGenres() != null && !movie.getGenres().isEmpty()
                ? movie.getGenres().stream()
                .map(Genre::getName) // Genre 엔티티에 getName() 메서드 있다고 가정
                .collect(Collectors.joining(", "))
                : null;

        this.rating = movie.getAgeRating(); // ✅ 필드명 일치
        this.runtime = movie.getRuntimeMinutes() != null
                ? movie.getRuntimeMinutes() + "분"
                : null;
    }

    /** 선택사항: 정적 팩토리 메서드 */
    public static MovieResponseDto from(Movie movie) {
        return new MovieResponseDto(movie);
    }
}
