package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.domain.MovieOtt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieOttRepository extends JpaRepository<MovieOtt, Long> {
    List<MovieOtt> findByMovieAndRegion(Movie movie, String region);
    List<MovieOtt> findByMovie(Movie movie);
}

