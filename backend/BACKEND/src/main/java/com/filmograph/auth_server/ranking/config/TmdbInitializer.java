package com.filmograph.auth_server.ranking.config;

import com.filmograph.auth_server.ranking.service.TmdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 TMDB 랭킹 데이터 자동 로드
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbInitializer implements ApplicationRunner {

    private final TmdbService tmdbService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("🚀 TMDB 랭킹 데이터 초기화 시작...");

        // 비동기로 실행하여 애플리케이션 시작 속도에 영향 없도록
        new Thread(() -> {
            try {
                Thread.sleep(5000); // DB 연결 대기 시간 (5초로 증가)

                // GLOBAL 랭킹
                try {
                    log.info("GLOBAL 랭킹 갱신 시작...");
                    int globalCount = tmdbService.refreshGlobal();
                    log.info("✅ TMDB GLOBAL 랭킹 초기화 완료: {} items", globalCount);
                } catch (Exception e) {
                    log.error("❌ TMDB GLOBAL 랭킹 초기화 실패", e);
                    e.printStackTrace();
                }

                Thread.sleep(1000);

                // KR 랭킹
                try {
                    log.info("KR 랭킹 갱신 시작...");
                    int krCount = tmdbService.refreshKorea();
                    log.info("✅ TMDB KR 랭킹 초기화 완료: {} items", krCount);
                } catch (Exception e) {
                    log.error("❌ TMDB KR 랭킹 초기화 실패", e);
                    e.printStackTrace();
                }

                Thread.sleep(1000);

                // US 랭킹
                try {
                    log.info("US 랭킹 갱신 시작...");
                    int usCount = tmdbService.refreshUS();
                    log.info("✅ TMDB US 랭킹 초기화 완료: {} items", usCount);
                } catch (Exception e) {
                    log.error("❌ TMDB US 랭킹 초기화 실패", e);
                    e.printStackTrace();
                }

                log.info("🎉 TMDB 랭킹 데이터 초기화 완료!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("❌ TMDB 랭킹 초기화 스레드 중단", e);
            } catch (Exception e) {
                log.error("❌ TMDB 랭킹 초기화 실패", e);
                e.printStackTrace();
            }
        }).start();
    }
}
