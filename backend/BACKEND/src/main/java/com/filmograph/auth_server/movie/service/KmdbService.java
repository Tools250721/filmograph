package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.dto.MovieResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KmdbService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.api.kmdb.key}")
    private String kmdbKey;

    // KMDB 영화 검색
    public List<MovieResponseDto> searchMovies(String keyword, String year) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString("https://api.koreafilm.or.kr/openapi-data2/wisenut/search_api/search_json2.jsp")
                    .queryParam("collection", "kmdb_new2")
                    .queryParam("ServiceKey", kmdbKey)
                    .queryParam("query",keyword); //  인코딩하지 말 것 - 언어 자유롭게 검색하기 위함 ! title보다 query가 더 다양함 !!

            // 연도 필터 추가
            if (year != null && year.matches("\\d{4}")) {
                builder.queryParam("releaseDts", year + "0101");
                builder.queryParam("releaseDte", year + "1231");
            }

            String url = builder.toUriString();
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);

            // Data / Result 구조 유연하게 처리
            JsonNode results = null;
            JsonNode dataArray = root.path("Data");
            if (dataArray.isArray() && !dataArray.isEmpty()) {
                results = dataArray.get(0).path("Result");
            }
            if (results == null || results.isMissingNode() || !results.isArray() || results.isEmpty()) {
                results = root.path("Result"); // 루트 바로 하위에 Result 있을 때
            }
            if (results == null || !results.isArray() || results.isEmpty()) {
                return List.of();
            }

            //  영화 데이터 파싱
            List<MovieResponseDto> movies = new ArrayList<>();

            for (JsonNode node : results) {
                movies.add(MovieResponseDto.builder()
                        .title(safeText(node, "title"))
                        .releaseDate(getReleaseDate(node))
                        .nation(getNation(node))
                        .genre(safeText(node, "genre"))
                        .runtime(safeText(node, "runtime"))
                        .rating(safeText(node, "rating"))
                        .director(getDirector(node))
                        .plot(getPlot(node))
                        .posterUrl(getPoster(node))
                        .actors(getActors(node))
                        .build());
            }

            return movies;

        } catch (Exception e) {
            return List.of();
        }
    }

    // 제목으로 단일 영화 검색
    public MovieResponseDto findOneByTitle(String title) {
        List<MovieResponseDto> list = searchMovies(title, null);
        return list.isEmpty() ? null : list.get(0);
    }

    //  헬퍼 메서드

    private String safeText(JsonNode node, String field) {
        String text = node.path(field).asText("");
        return text.isBlank() ? null : text;
    }

    private String getDirector(JsonNode node) {
        try {
            JsonNode directors = node.path("directors").path("director");
            if (directors.isArray()) {
                List<String> names = new ArrayList<>();
                for (JsonNode d : directors) {
                    String name = d.path("directorNm").asText("");
                    if (!name.isBlank()) names.add(name);
                }
                return String.join(", ", names);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getActors(JsonNode node) {
        try {
            JsonNode arr = node.path("actors").path("actor");
            if (arr.isArray()) {
                List<String> names = new ArrayList<>();
                for (JsonNode a : arr) {
                    String name = a.path("actorNm").asText("");
                    if (!name.isBlank()) names.add(name);
                }
                return String.join(", ", names);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getNation(JsonNode node) {
        String nations = node.path("nation").asText("");
        if (nations.isBlank()) return null;
        return String.join(", ", nations.split("\\|"));
    }

    private String getReleaseDate(JsonNode node) {
        String date = node.path("repRlsDate").asText("");
        if (date.length() == 8) {
            return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
        }
        return date.isBlank() ? null : date;
    }

    private String getPlot(JsonNode node) {
        JsonNode plotArray = node.path("plots").path("plot");
        if (plotArray.isArray() && !plotArray.isEmpty()) {
            return plotArray.get(0).path("plotText").asText("");
        }
        return null;
    }

    private String getPoster(JsonNode node) {
        String posters = node.path("posters").asText("");
        if (posters.contains("|")) posters = posters.split("\\|")[0];
        return posters.isBlank() ? null : posters;
    }
}
