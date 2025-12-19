package com.filmograph.auth_server.ranking.provider.repo;

import com.filmograph.auth_server.ranking.provider.domain.ProviderTrendingItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderTrendingItemRepo extends JpaRepository<ProviderTrendingItem, Long> {}
