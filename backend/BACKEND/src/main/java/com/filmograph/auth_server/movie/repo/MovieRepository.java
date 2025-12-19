package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTitle(String title);

    Optional<Movie> findByTmdbId(Long tmdbId);
}
