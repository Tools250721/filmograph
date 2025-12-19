package com.filmograph.auth_server.ranking.provider.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbFetcher {

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final RestClient client = RestClient.builder()
            .baseUrl("https://api.themoviedb.org/3")
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .build();

    public List<Map<String, Object>> fetchTrendingGlobal() {
        String path = "/trending/movie/day?api_key=" + apiKey + "&language=ko-KR";
        return fetch(path);
    }

    public List<Map<String, Object>> fetchPopularKR() {
        String path = "/movie/popular?api_key=" + apiKey + "&language=ko-KR&region=KR";
        return fetch(path);
    }

    public List<Map<String, Object>> fetchPopularUS() {
        String path = "/movie/popular?api_key=" + apiKey + "&language=en-US&region=US";
        return fetch(path);
    }

    private List<Map<String, Object>> fetch(String path) {
        try {
            log.info("TMDb API 호출 시작: {}", path);
            String body = client.get().uri(path).retrieve().body(String.class);
            
            if (body == null || body.isEmpty()) {
                log.error("TMDb API 응답이 비어있습니다: {}", path);
                return List.of();
            }
            
            JsonNode root = mapper.readTree(body);
            JsonNode results = root.path("results");

            List<Map<String, Object>> out = new ArrayList<>();
            if (results.isArray()) {
                for (JsonNode n : results) {
                    // 🔹 title 없으면 name 사용
                    String title = n.hasNonNull("title") ? n.get("title").asText()
                            : n.hasNonNull("name") ? n.get("name").asText()
                            : "Untitled";

                    String posterPath = n.hasNonNull("poster_path") ? n.get("poster_path").asText() : "";

                    out.add(Map.of(
                            "id", n.path("id").asInt(),
                            "title", title,
                            "poster_path", posterPath
                    ));
                }
            }
            log.info("✅ Fetched {} items from TMDb (path={})", out.size(), path);
            return out;
        } catch (Exception e) {
            log.error("❌ TMDb fetch error (path={}): {}", path, e.getMessage(), e);
            e.printStackTrace();
            return List.of();
        }
    }
}
