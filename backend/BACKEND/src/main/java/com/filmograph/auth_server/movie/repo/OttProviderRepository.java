package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.OttProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OttProviderRepository extends JpaRepository<OttProvider, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<OttProvider> findByName(String name);
}

