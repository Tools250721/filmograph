package com.filmograph.auth_server.ranking.provider.service;

import com.filmograph.auth_server.ranking.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ProviderSchedule {

    private final TmdbService tmdbService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void refreshGlobal() {
        tmdbService.refreshGlobal();
    }

    @Scheduled(cron = "0 5 3 * * *", zone = "Asia/Seoul")
    public void refreshKorea() {
        tmdbService.refreshKorea();
    }

    @Scheduled(cron = "0 10 3 * * *", zone = "Asia/Seoul")
    public void refreshUS() {
        tmdbService.refreshUS();
    }
}
