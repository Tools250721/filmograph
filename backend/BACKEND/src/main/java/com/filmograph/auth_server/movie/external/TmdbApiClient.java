package com.filmograph.auth_server.movie.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * TMDB (The Movie Database) API 클라이언트
 * https://www.themoviedb.org/documentation/api
 */
@Component
public class TmdbApiClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final String imageBaseUrl;

    public TmdbApiClient(
            @Value("${tmdb.api.key}") String apiKey,
            @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}") String baseUrl,
            @Value("${tmdb.api.image-base-url:https://image.tmdb.org/t/p}") String imageBaseUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.imageBaseUrl = imageBaseUrl;
    }

    /**
     * 영화 검색
     */
    public TmdbSearchResponse searchMovies(String query, int page) {
        // API 키가 설정되지 않았으면 예외 발생
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) {
            throw new IllegalStateException("TMDB API 키가 설정되지 않았습니다. application.yml에서 tmdb.api.key를 설정하세요.");
        }
        
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("language", "ko-KR")
                .toUriString();

        return restTemplate.getForObject(url, TmdbSearchResponse.class);
    }

    /**
     * 영화 상세 정보 조회
     */
    public TmdbMovieDetail getMovieDetail(Long tmdbId) {
        // API 키가 설정되지 않았으면 예외 발생
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) {
            throw new IllegalStateException("TMDB API 키가 설정되지 않았습니다. application.yml에서 tmdb.api.key를 설정하세요.");
        }
        
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/" + tmdbId)
                .queryParam("api_key", apiKey)
                .queryParam("language", "ko-KR")
                .queryParam("append_to_response", "credits,videos,watch/providers")
                .toUriString();

        return restTemplate.getForObject(url, TmdbMovieDetail.class);
    }

    /**
     * 영화 OTT 제공 정보 조회 (watch/providers)
     */
    public TmdbWatchProviders getWatchProviders(Long tmdbId, String region) {
        // API 키가 설정되지 않았으면 예외 발생
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) {
            throw new IllegalStateException("TMDB API 키가 설정되지 않았습니다. application.yml에서 tmdb.api.key를 설정하세요.");
        }
        
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/" + tmdbId + "/watch/providers")
                .queryParam("api_key", apiKey)
                .toUriString();

        return restTemplate.getForObject(url, TmdbWatchProviders.class);
    }

    /**
     * 영화 이미지 조회 (스틸컷, 포스터 등)
     */
    public TmdbMovieImages getMovieImages(Long tmdbId) {
        // API 키가 설정되지 않았으면 예외 발생
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) {
            throw new IllegalStateException("TMDB API 키가 설정되지 않았습니다. application.yml에서 tmdb.api.key를 설정하세요.");
        }
        
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/" + tmdbId + "/images")
                .queryParam("api_key", apiKey)
                .toUriString();

        return restTemplate.getForObject(url, TmdbMovieImages.class);
    }

    /**
     * 인기 영화 목록
     */
    public TmdbSearchResponse getPopularMovies(int page) {
        // API 키가 설정되지 않았으면 예외 발생
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) {
            throw new IllegalStateException("TMDB API 키가 설정되지 않았습니다. application.yml에서 tmdb.api.key를 설정하세요.");
        }
        
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/popular")
                .queryParam("api_key", apiKey)
                .queryParam("page", page)
                .queryParam("language", "ko-KR")
                .toUriString();

        return restTemplate.getForObject(url, TmdbSearchResponse.class);
    }

    /**
     * 트렌딩 영화 목록 (day 또는 week)
     */
    public TmdbSearchResponse getTrendingMovies(String timeWindow, int page) {
        // API 키가 설정되지 않았으면 예외 발생
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) {
            throw new IllegalStateException("TMDB API 키가 설정되지 않았습니다. application.yml에서 tmdb.api.key를 설정하세요.");
        }
        
        // timeWindow는 "day" 또는 "week"
        if (!timeWindow.equals("day") && !timeWindow.equals("week")) {
            throw new IllegalArgumentException("timeWindow는 'day' 또는 'week'여야 합니다.");
        }
        
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/trending/movie/" + timeWindow)
                .queryParam("api_key", apiKey)
                .queryParam("page", page)
                .queryParam("language", "ko-KR")
                .toUriString();

        return restTemplate.getForObject(url, TmdbSearchResponse.class);
    }

    /**
     * Top Rated 영화 목록
     */
    public TmdbSearchResponse getTopRatedMovies(int page) {
        // API 키가 설정되지 않았으면 예외 발생
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) {
            throw new IllegalStateException("TMDB API 키가 설정되지 않았습니다. application.yml에서 tmdb.api.key를 설정하세요.");
        }
        
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/top_rated")
                .queryParam("api_key", apiKey)
                .queryParam("page", page)
                .queryParam("language", "ko-KR")
                .toUriString();

        return restTemplate.getForObject(url, TmdbSearchResponse.class);
    }

    /**
     * 현재 상영 중인 영화 목록
     */
    public TmdbSearchResponse getNowPlayingMovies(int page) {
        // API 키가 설정되지 않았으면 예외 발생
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) {
            throw new IllegalStateException("TMDB API 키가 설정되지 않았습니다. application.yml에서 tmdb.api.key를 설정하세요.");
        }
        
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/now_playing")
                .queryParam("api_key", apiKey)
                .queryParam("page", page)
                .queryParam("language", "ko-KR")
                .queryParam("region", "KR")
                .toUriString();

        return restTemplate.getForObject(url, TmdbSearchResponse.class);
    }

    public String getImageUrl(String path, String size) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (path.startsWith("http")) {
            return path;
        }
        return imageBaseUrl + "/" + size + path;
    }

    // ===== DTO 클래스들 =====

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbSearchResponse {
        private int page;
        private List<TmdbMovie> results;
        private int totalPages;
        private int totalResults;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbMovie {
        private Long id;
        private String title;
        @JsonProperty("original_title")
        private String originalTitle;
        private String overview;
        @JsonProperty("release_date")
        private String releaseDate;
        @JsonProperty("poster_path")
        private String posterPath;
        @JsonProperty("backdrop_path")
        private String backdropPath;
        @JsonProperty("vote_average")
        private Double voteAverage;
        @JsonProperty("vote_count")
        private Integer voteCount;
        private List<TmdbGenre> genres;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @lombok.EqualsAndHashCode(callSuper = false)
    public static class TmdbMovieDetail extends TmdbMovie {
        @JsonProperty("runtime")
        private Integer runtime;
        @JsonProperty("production_countries")
        private List<Map<String, String>> productionCountries;
        @JsonProperty("credits")
        private TmdbCredits credits;
        @JsonProperty("watch/providers")
        private TmdbWatchProviders watchProviders;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbGenre {
        private Long id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbCredits {
        private List<TmdbPerson> cast;
        private List<TmdbPerson> crew;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbPerson {
        private Long id;
        private String name;
        @JsonProperty("profile_path")
        private String profilePath;
        private String character;  // 배우인 경우
        private String job;  // 감독인 경우
        @JsonProperty("known_for_department")
        private String knownForDepartment;
        @JsonProperty("order")
        private Integer order;  // 배우 순서
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbWatchProviders {
        @JsonProperty("results")
        private Map<String, TmdbWatchProvidersData> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbWatchProvidersData {
        @JsonProperty("link")
        private String link;
        @JsonProperty("flatrate")
        private List<TmdbProvider> flatrate;  // 구독 서비스
        @JsonProperty("rent")
        private List<TmdbProvider> rent;  // 대여
        @JsonProperty("buy")
        private List<TmdbProvider> buy;  // 구매
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbProvider {
        @JsonProperty("logo_path")
        private String logoPath;
        @JsonProperty("provider_id")
        private Integer providerId;
        @JsonProperty("provider_name")
        private String providerName;
        @JsonProperty("display_priority")
        private Integer displayPriority;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbMovieImages {
        private List<TmdbImage> backdrops;
        private List<TmdbImage> posters;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TmdbImage {
        @JsonProperty("file_path")
        private String filePath;
        private Double aspectRatio;
        private Integer height;
        private Integer width;
        @JsonProperty("iso_639_1")
        private String iso6391;
        @JsonProperty("vote_average")
        private Double voteAverage;
        @JsonProperty("vote_count")
        private Integer voteCount;
    }
}

