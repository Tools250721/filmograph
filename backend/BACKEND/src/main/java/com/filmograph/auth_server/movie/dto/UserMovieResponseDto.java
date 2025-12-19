package com.filmograph.auth_server.movie.dto;

import com.filmograph.auth_server.movie.domain.Movie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserMovieResponseDto extends MovieResponseDto {
    private LocalDateTime createdAt;

    public UserMovieResponseDto(Movie movie, LocalDateTime createdAt) {
        super(movie);
        this.createdAt = createdAt;
    }
}

