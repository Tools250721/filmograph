package com.filmograph.auth_server.ranking.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class NetflixSchedule {
    private final NetflixTop10Ingestor ingestor;
    public NetflixSchedule(NetflixTop10Ingestor ingestor){ this.ingestor = ingestor; }

    // 매주 화요일 21:00 KST
    @Scheduled(cron="0 0 21 ? * TUE", zone="Asia/Seoul")
    public void run() throws Exception { ingestor.ingest(); }
}
