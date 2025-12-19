package com.filmograph.auth_server.ranking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TmdbSchedule {

    private final TmdbService tmdbService;

    // 🌍 글로벌 트렌딩
    @Scheduled(cron = "${tmdb.cron.global}", zone = "Asia/Seoul")
    public void refreshGlobal() {
        int saved = tmdbService.refreshGlobal();
        log.info("✅ TMDb GLOBAL 자동 갱신: {} items 저장됨", saved);
    }

    // 🇰🇷 한국 인기
    @Scheduled(cron = "${tmdb.cron.kr}", zone = "Asia/Seoul")
    public void refreshKorea() {
        int saved = tmdbService.refreshKorea();
        log.info("✅ TMDb KR 자동 갱신: {} items 저장됨", saved);
    }

    // 🇺🇸 미국 인기
    @Scheduled(cron = "${tmdb.cron.us}", zone = "Asia/Seoul")
    public void refreshUS() {
        int saved = tmdbService.refreshUS();
        log.info("✅ TMDb US 자동 갱신: {} items 저장됨", saved);
    }
}
