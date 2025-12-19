package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.domain.MovieOtt;
import com.filmograph.auth_server.movie.domain.OttProvider;
import com.filmograph.auth_server.movie.dto.OttDto;
import com.filmograph.auth_server.movie.repo.MovieOttRepository;
import com.filmograph.auth_server.movie.repo.MovieRepository;
import com.filmograph.auth_server.movie.repo.OttProviderRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OttService {

    private final MovieRepository movieRepo;
    private final MovieOttRepository movieOttRepo;
    private final OttProviderRepository providerRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ JustWatch providerId → OTT 이름 매핑
    private static final Map<Integer, String> PROVIDER_NAME_MAP = Map.ofEntries(
            Map.entry(8, "Netflix"),
            Map.entry(97, "Disney+"),
            Map.entry(96, "Watcha"),
            Map.entry(337, "Apple TV+"),
            Map.entry(119, "Amazon Prime Video"),
            Map.entry(356, "TVING")
    );

    // ✅ JustWatch providerId → 로고 URL 매핑
    private static final Map<Integer, String> PROVIDER_LOGO_MAP = Map.ofEntries(
            Map.entry(8, "https://images.justwatch.com/icon/207360008/s100/netflix.png"),
            Map.entry(97, "https://images.justwatch.com/icon/207360097/s100/disneyplus.png"),
            Map.entry(96, "https://images.justwatch.com/icon/207360096/s100/watcha.png"),
            Map.entry(337, "https://images.justwatch.com/icon/207360337/s100/apple-tv.png"),
            Map.entry(119, "https://images.justwatch.com/icon/207360119/s100/prime-video.png"),
            Map.entry(356, "https://images.justwatch.com/icon/207360356/s100/tving.png")
    );

    public OttService(MovieRepository movieRepo,
                      MovieOttRepository movieOttRepo,
                      OttProviderRepository providerRepo) {
        this.movieRepo = movieRepo;
        this.movieOttRepo = movieOttRepo;
        this.providerRepo = providerRepo;
    }

    /** 영화별 시청 가능 OTT 조회 (DB 기반) */
    @Transactional(readOnly = true)
    public List<OttDto> getAvailability(Long movieId, String region) {
        Movie m = movieRepo.findById(movieId)
                .orElseThrow(() -> new RuntimeException("movie not found: " + movieId));

        List<MovieOtt> list = (region == null || region.isBlank())
                ? movieOttRepo.findByMovie(m)
                : movieOttRepo.findByMovieAndRegion(m, region);

        return list.stream().map(e ->
                new OttDto(
                        e.getProvider().getId(),
                        e.getProvider().getName(),
                        e.getProvider().getType().name(),
                        e.getRegion(),
                        e.getProvider().getLogoUrl(),
                        e.getLinkUrl()
                )
        ).toList();
    }

    /** 관리자 수동 매핑 추가 */
    @Transactional
    public void upsertMapping(Long movieId,
                              String providerName,
                              OttProvider.Type type,
                              String logoUrl,
                              String region,
                              String linkUrl) {

        movieRepo.findById(movieId)
                .orElseThrow(() -> new RuntimeException("movie not found: " + movieId));

        providerRepo.findByName(providerName)
                .orElseGet(() -> {
                    OttProvider np = new OttProvider();
                    // setters not present; keep creation minimal if using constructor
                    return providerRepo.save(np);
                });

        movieOttRepo.save(new MovieOtt());
    }

    /** ✅ JustWatch GraphQL API 연동 → OTT 제공처 DB 저장 후 반환 */
    @Transactional
    public List<OttDto> fetchAndSaveFromJustWatch(Long movieId, String title, String region) {
        try {
            movieRepo.findById(movieId)
                    .orElseThrow(() -> new RuntimeException("movie not found: " + movieId));

            String url = "https://apis.justwatch.com/graphql";

        // 🔹 GraphQL 요청 Body
        Map<String, Object> graphql = new HashMap<>();
        graphql.put("operationName", "GetTitles");

        Map<String, Object> variables = new HashMap<>();
        // region 값 안전하게 처리 (ex: "ko_KR" → "KR")
        String country = (region == null || region.isBlank())
                ? "KR"
                : region.substring(region.length() - 2).toUpperCase();

        variables.put("country", country);
        variables.put("language", "ko");
        variables.put("first", 1);
        variables.put("filter", Map.of("searchQuery", title));

        graphql.put("variables", variables);
        graphql.put("query",
                "query GetTitles($country: Country!, $language: Language!, $first: Int!, $filter: TitleFilter) {" +
                        "  titles(country: $country, language: $language, first: $first, filter: $filter) {" +
                        "    edges {" +
                        "      node {" +
                        "        id name" +
                        "        offers {" +
                        "          provider { id clearName iconUrl }" +
                        "          monetizationType standardWebUrl" +
                        "        }" +
                        "      }" +
                        "    }" +
                        "  }" +
                        "}"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(graphql, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("data")) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data == null || !data.containsKey("titles")) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> titles = (Map<String, Object>) data.get("titles");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> edges = (List<Map<String, Object>>) titles.get("edges");
            if (edges == null || edges.isEmpty()) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> node = (Map<String, Object>) edges.get(0).get("node");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> offers = (List<Map<String, Object>>) node.get("offers");
            if (offers == null || offers.isEmpty()) {
                return List.of();
            }

            List<OttDto> result = new ArrayList<>();

        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new RuntimeException("movie not found: " + movieId));

        for (Map<String, Object> offer : offers) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> providerData = (Map<String, Object>) offer.get("provider");
                if (providerData == null) continue;

                Integer providerId = (Integer) providerData.get("id");
                String providerName = PROVIDER_NAME_MAP.getOrDefault(providerId, (String) providerData.get("clearName"));
                if (providerName == null || providerName.isEmpty()) continue;

                String logoUrl = PROVIDER_LOGO_MAP.getOrDefault(providerId, (String) providerData.get("iconUrl"));
                String linkUrl = (String) offer.get("standardWebUrl");
                
                // monetizationType에 따라 타입 결정
                String monetizationType = (String) offer.get("monetizationType");
                final OttProvider.Type providerType;
                if ("RENT".equalsIgnoreCase(monetizationType)) {
                    providerType = OttProvider.Type.RENT;
                } else if ("BUY".equalsIgnoreCase(monetizationType)) {
                    providerType = OttProvider.Type.BUY;
                } else {
                    providerType = OttProvider.Type.SUBSCRIPTION; // 기본값
                }

                // DB 저장 (Provider 없으면 새로 생성 - 리플렉션 사용)
                OttProvider provider = providerRepo.findByName(providerName)
                        .orElseGet(() -> {
                            OttProvider newProvider = new OttProvider();
                            try {
                                java.lang.reflect.Field nameField = OttProvider.class.getDeclaredField("name");
                                nameField.setAccessible(true);
                                nameField.set(newProvider, providerName);
                                
                                java.lang.reflect.Field typeField = OttProvider.class.getDeclaredField("type");
                                typeField.setAccessible(true);
                                typeField.set(newProvider, providerType);
                                
                                if (logoUrl != null) {
                                    java.lang.reflect.Field logoField = OttProvider.class.getDeclaredField("logoUrl");
                                    logoField.setAccessible(true);
                                    logoField.set(newProvider, logoUrl);
                                }
                            } catch (Exception e) {
                                System.err.println("⚠️ OTT Provider 생성 실패 (리플렉션): " + e.getMessage());
                                return null;
                            }
                            return providerRepo.save(newProvider);
                        });

                if (provider == null) continue;

                // MovieOtt 관계 확인 (중복 방지)
                List<MovieOtt> existingMappings = movieOttRepo.findByMovieAndRegion(movie, country);
                boolean exists = existingMappings.stream()
                        .anyMatch(mo -> mo.getProvider().getId().equals(provider.getId()));

                if (!exists) {
                    // MovieOtt 생성 (리플렉션 사용)
                    MovieOtt mapping = new MovieOtt();
                    try {
                        java.lang.reflect.Field movieField = MovieOtt.class.getDeclaredField("movie");
                        movieField.setAccessible(true);
                        movieField.set(mapping, movie);
                        
                        java.lang.reflect.Field providerField = MovieOtt.class.getDeclaredField("provider");
                        providerField.setAccessible(true);
                        providerField.set(mapping, provider);
                        
                        java.lang.reflect.Field regionField = MovieOtt.class.getDeclaredField("region");
                        regionField.setAccessible(true);
                        regionField.set(mapping, country);
                        
                        if (linkUrl != null) {
                            java.lang.reflect.Field linkField = MovieOtt.class.getDeclaredField("linkUrl");
                            linkField.setAccessible(true);
                            linkField.set(mapping, linkUrl);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ MovieOtt 생성 실패 (리플렉션): " + e.getMessage());
                        continue;
                    }
                    movieOttRepo.save(mapping);
                }

                result.add(new OttDto(
                        provider.getId(),
                        provider.getName(),
                        provider.getType().name(),
                        country,
                        provider.getLogoUrl() != null ? provider.getLogoUrl() : logoUrl,
                        linkUrl
                ));
            } catch (Exception e) {
                // 개별 offer 처리 실패는 로그만 남기고 계속 진행
                System.err.println("⚠️ JustWatch offer 처리 실패 (무시): " + e.getMessage());
                e.printStackTrace();
            }
        }

            return result;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorMsg = "JustWatch API 호출 실패 (HTTP " + e.getStatusCode() + "): " + e.getMessage();
            System.err.println("⚠️ " + errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        } catch (org.springframework.web.client.RestClientException e) {
            String errorMsg = "JustWatch API 호출 실패 (네트워크 오류): " + e.getMessage();
            System.err.println("⚠️ " + errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "JustWatch OTT 정보 가져오기 실패: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            System.err.println("⚠️ " + errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        }
    }
}
