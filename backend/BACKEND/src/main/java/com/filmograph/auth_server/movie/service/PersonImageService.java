package com.filmograph.auth_server.movie.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PersonImageService {

    @Value("${tmdb.api.key}")
    private String TMDB_API_KEY;

    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/person";
    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";
    private static final String DEFAULT_IMAGE_URL = "https://your-cdn.com/images/default_person.png";
    // ✅ 배우/감독 공용 기본 이미지 (CDN/S3 경로로 교체 가능)

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 🎬 인물(배우·감독) 이름으로 TMDb에서 이미지 검색 후 URL 반환
     * TMDb에 없으면 기본 이미지 반환
     */
    public String getPersonImage(String name) {
        try {
            String url = UriComponentsBuilder.fromUriString(TMDB_SEARCH_URL)
                    .queryParam("api_key", TMDB_API_KEY)
                    .queryParam("query", name)
                    .toUriString();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("results")) {
                log.warn("❌ TMDb 응답 없음: {}", name);
                return DEFAULT_IMAGE_URL;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results.isEmpty()) {
                log.warn("⚠️ 인물 검색 결과 없음: {}", name);
                return DEFAULT_IMAGE_URL;
            }

            Object profilePath = results.get(0).get("profile_path");
            if (profilePath != null) {
                String imageUrl = TMDB_IMAGE_BASE + profilePath.toString();
                log.info("✅ TMDb 이미지 불러옴: {} → {}", name, imageUrl);
                return imageUrl;
            } else {
                log.warn("⚠️ TMDb에 프로필 이미지 없음: {}", name);
                return DEFAULT_IMAGE_URL;
            }

        } catch (Exception e) {
            log.error("🚨 TMDb 인물 이미지 요청 실패: {} / {}", name, e.getMessage());
            return DEFAULT_IMAGE_URL;
        }
    }
}
