package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.Movie;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * KMDB 영화 검색 API 서비스
 */
@Service
@RequiredArgsConstructor
public class MovieApiService {

    private final RestTemplate restTemplate;

    @Value("${app.api.kmdb.key}")
    private String KMDB_KEY;

    // DB 검색용 (사용하지 않음)
    public List<Movie> searchMovies(String keyword) {
        return new ArrayList<>();
    }

    // KMDB API 검색
    public List<Movie> searchMoviesFromKmdb(String keyword) {
        String url = "http://api.koreafilm.or.kr/openapi-data2/wisenut/search_api/search_json2.jsp"
                + "?collection=kmdb_new2"
                + "&ServiceKey=" + KMDB_KEY
                + "&title=" + keyword;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        List<Movie> movies = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode results = root.path("Data").get(0).path("Result");
            for (JsonNode result : results) {
                String title = result.path("title").asText();
                String prodYear = result.path("prodYear").asText();
                String director = result.path("directors").path("director").get(0).path("directorNm").asText();
                String poster = result.path("posters").asText().split("\\|")[0];

                Movie movie = Movie.builder()
                        .title(title)
                        .releaseDate(String.valueOf(Integer.valueOf(prodYear)))
                        .director(director)
                        .posterUrl(poster)
                        .build();

                movies.add(movie);
            }
        } catch (Exception e) {
            throw new RuntimeException("KMDB API 파싱 중 오류 발생", e);
        }

        return movies;
    }
}
