package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    Optional<Actor> findByName(String name);
}
