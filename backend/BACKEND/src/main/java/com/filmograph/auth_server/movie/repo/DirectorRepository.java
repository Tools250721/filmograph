package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Director;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, Long> {
    Optional<Director> findByName(String name);
}
