package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.dto.KmdbMovieDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class KmdbClient {

    private final RestTemplate restTemplate;

    @Value("${app.api.kmdb.key}")
    private String apiKey;

    public KmdbMovieDto searchByTitle(String title) {
        String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String url = "http://api.koreafilm.or.kr/openapi-data2/wisenut/search_api/search_json.jsp"
                + "?collection=kmdb_new2"
                + "&title=" + encoded
                + "&ServiceKey=" + apiKey;

        return restTemplate.getForObject(url, KmdbMovieDto.class);
    }
}
