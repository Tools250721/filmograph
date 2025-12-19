package com.filmograph.auth_server.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filmograph.auth_server.movie.dto.MovieResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KobisService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.api.kobis.key}")
    private String kobisKey;

    public List<MovieResponseDto> searchMovies(String keyword) {
        try {
            String url = UriComponentsBuilder
                    .fromUriString("https://kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieList.json")
                    .queryParam("key", kobisKey)
                    .queryParam("movieNm", URLEncoder.encode(keyword, StandardCharsets.UTF_8))
                    .toUriString();

            String json = restTemplate.getForObject(url, String.class);
            JsonNode movieList = objectMapper.readTree(json)
                    .path("movieListResult").path("movieList");

            List<MovieResponseDto> list = new ArrayList<>();
            for (JsonNode node : movieList) {
                list.add(MovieResponseDto.builder()
                        .title(node.path("movieNm").asText())
                        .releaseDate(node.path("openDt").asText())
                        .director(node.path("directors").isArray() && node.path("directors").size() > 0
                                ? node.path("directors").get(0).path("peopleNm").asText("")
                                : "")
                        .nation(node.path("nationAlt").asText(""))
                        .build());
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
