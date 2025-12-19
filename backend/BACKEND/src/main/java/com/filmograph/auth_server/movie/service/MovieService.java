package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.domain.MovieOtt;
import com.filmograph.auth_server.movie.domain.OttProvider;
import com.filmograph.auth_server.movie.dto.*;
import com.filmograph.auth_server.movie.external.TmdbApiClient;
import com.filmograph.auth_server.movie.repo.MovieRepository;
import com.filmograph.auth_server.movie.repo.MovieOttRepository;
import com.filmograph.auth_server.movie.repo.OttProviderRepository;
import com.filmograph.auth_server.movie.repo.RatingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository repo;
    private final OttService ottService;
    private final RatingRepository ratingRepository;
    private final TmdbApiClient tmdbApiClient;
    private final OttProviderRepository ottProviderRepository;
    private final MovieOttRepository movieOttRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public MovieDetailDto getDetail(Long id) {
        // DB에서 조회 (로컬 DB ID로 조회)
        Movie m = repo.findById(id).orElse(null);
        
        // DB에 없으면 에러 반환
        // 주의: 로컬 DB ID와 TMDB ID는 다릅니다!
        // TMDB에서 영화를 가져오려면 영화 검색 API를 사용하세요.
        if (m == null) {
            log.warn("DB에서 영화를 찾을 수 없음: movieId={}", id);
            throw new RuntimeException("movie not found: " + id);
        }
        
        // 장르 정보 조회 (트랜잭션 내에서 지연 로딩 가능)
        List<NamedDto> genres = m.getGenres().stream()
                .map(g -> new NamedDto(g.getId(), g.getName()))
                .toList();
        
        // TMDB에서 배우/감독 및 OTT 정보 가져오기
        Long tmdbId = m.getTmdbId();
        List<PersonDto> directors = java.util.List.of();
        List<ActorDto> actors = java.util.List.of();
        List<OttDto> ott = ottService.getAvailability(id, null);  // DB에서 먼저 조회
        List<ImageDto> stills = java.util.List.of();  // 스틸컷 이미지
        
        // TMDB ID가 있으면 TMDB에서 추가 정보 가져오기
        if (tmdbId != null) {
            try {
                log.debug("TMDB에서 추가 정보 조회: tmdbId={}", tmdbId);
                TmdbApiClient.TmdbMovieDetail tmdbDetail = tmdbApiClient.getMovieDetail(tmdbId);
                
                // 감독 정보 (crew에서 Director 찾기)
                if (tmdbDetail.getCredits() != null && tmdbDetail.getCredits().getCrew() != null) {
                    directors = tmdbDetail.getCredits().getCrew().stream()
                            .filter(person -> "Directing".equals(person.getKnownForDepartment()) 
                                    || "Director".equals(person.getJob()))
                            .collect(java.util.stream.Collectors.toMap(
                                    TmdbApiClient.TmdbPerson::getId,  // ID를 키로 사용
                                    person -> person,  // 값은 person 객체
                                    (existing, replacement) -> existing  // 중복 시 기존 것 유지
                            ))
                            .values()
                            .stream()
                            .limit(5)  // 최대 5명
                            .map(person -> new PersonDto(
                                    person.getId(),
                                    person.getName(),
                                    tmdbApiClient.getImageUrl(person.getProfilePath(), "w185")
                            ))
                            .toList();
                }
                
                // 배우 정보 (cast에서 상위 10명)
                if (tmdbDetail.getCredits() != null && tmdbDetail.getCredits().getCast() != null) {
                    actors = tmdbDetail.getCredits().getCast().stream()
                            .limit(10)  // 상위 10명
                            .map(person -> new ActorDto(
                                    person.getId(),
                                    person.getName(),
                                    person.getCharacter(),
                                    person.getOrder(),
                                    tmdbApiClient.getImageUrl(person.getProfilePath(), "w185")
                            ))
                            .toList();
                }
                
                // OTT 정보 (TMDB에서 가져오기, DB에 없으면)
                if (ott.isEmpty() && tmdbDetail.getWatchProviders() != null) {
                    // 한국(KR) 우선, 없으면 미국(US) 지역의 OTT 정보 가져오기
                    TmdbApiClient.TmdbWatchProviders watchProviders = tmdbDetail.getWatchProviders();
                    if (watchProviders != null && watchProviders.getResults() != null) {
                        // KR 우선, 없으면 US
                        String[] regions = {"KR", "US"};
                        for (String region : regions) {
                            TmdbApiClient.TmdbWatchProvidersData providersData = watchProviders.getResults().get(region);
                            if (providersData != null) {
                                // DB에 저장 시도 (백그라운드에서, 실패해도 무시)
                                try {
                                    saveOttProvidersToDb(id, providersData, region);
                                } catch (Exception e) {
                                    log.warn("OTT 정보 DB 저장 실패 (무시): movieId={}, error={}", id, e.getMessage());
                                }
                                // 응답용으로는 DTO로 변환
                                ott = convertTmdbProvidersToOttDto(providersData, tmdbApiClient);
                                break; // 첫 번째로 찾은 지역만 사용
                            }
                        }
                    }
                }
                
                // 스틸컷 이미지 가져오기 (backdrops 사용)
                try {
                    TmdbApiClient.TmdbMovieImages images = tmdbApiClient.getMovieImages(tmdbId);
                    if (images != null && images.getBackdrops() != null && !images.getBackdrops().isEmpty()) {
                        stills = images.getBackdrops().stream()
                                .limit(20)  // 최대 20개
                                .map(img -> new ImageDto(
                                        tmdbApiClient.getImageUrl(img.getFilePath(), "w1280"),  // 고해상도 이미지
                                        img.getAspectRatio(),
                                        img.getWidth(),
                                        img.getHeight()
                                ))
                                .toList();
                    }
                } catch (Exception e) {
                    log.warn("TMDB에서 이미지 정보를 가져오는 중 오류 발생: tmdbId={}, error={}", tmdbId, e.getMessage());
                    // 이미지 가져오기 실패해도 계속 진행
                }
            } catch (IllegalStateException e) {
                // TMDB API 키가 설정되지 않은 경우
                log.warn("TMDB API 키가 설정되지 않아 추가 정보를 가져올 수 없습니다: {}", e.getMessage());
                // 오류가 발생해도 DB 정보는 반환
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // TMDB API 호출 실패 (404, 401 등)
                log.warn("TMDB API 호출 실패: tmdbId={}, status={}, error={}", tmdbId, e.getStatusCode(), e.getMessage());
                // 오류가 발생해도 DB 정보는 반환
            } catch (Exception e) {
                log.warn("TMDB에서 추가 정보를 가져오는 중 오류 발생: tmdbId={}, error={}", tmdbId, e.getMessage());
                // 오류가 발생해도 DB 정보는 반환
            }
        }
        
        // 통계 정보 조회 (실제 데이터)
        Double avgRating = ratingRepository.findAverageRatingByMovieId(id).orElse(0.0);
        Long ratingCount = ratingRepository.countByMovieId(id);
        StatsDto stats = new StatsDto(avgRating, ratingCount);
        
        return new MovieDetailDto(
                m.getId(), m.getTitle(), m.getOriginalTitle(), m.getOverview(),
                m.getReleaseYear(), m.getRuntimeMinutes(), m.getCountry(), m.getAgeRating(),
                m.getPosterUrl(), m.getBackdropUrl(),
                genres,  // 실제 장르 데이터
                directors,  // TMDB에서 가져온 감독 정보
                actors,  // TMDB에서 가져온 배우 정보
                ott,  // OTT 정보 (DB 또는 TMDB)
                stats,  // 실제 통계 데이터
                stills  // 스틸컷 이미지 리스트
        );
    }
    
    /**
     * TMDB watch/providers 정보를 OttDto 리스트로 변환
     */
    private List<OttDto> convertTmdbProvidersToOttDto(
            TmdbApiClient.TmdbWatchProvidersData providersData,
            TmdbApiClient tmdbApiClient) {
        java.util.List<OttDto> result = new java.util.ArrayList<>();
        
        // 구독 서비스 (flatrate)
        if (providersData.getFlatrate() != null) {
            for (TmdbApiClient.TmdbProvider provider : providersData.getFlatrate()) {
                result.add(new OttDto(
                        provider.getProviderId().longValue(),
                        provider.getProviderName(),
                        "SUBSCRIPTION",
                        "KR",  // 기본값
                        tmdbApiClient.getImageUrl(provider.getLogoPath(), "w92"),
                        providersData.getLink()
                ));
            }
        }
        
        // 대여 (rent)
        if (providersData.getRent() != null) {
            for (TmdbApiClient.TmdbProvider provider : providersData.getRent()) {
                result.add(new OttDto(
                        provider.getProviderId().longValue(),
                        provider.getProviderName(),
                        "RENT",
                        "KR",
                        tmdbApiClient.getImageUrl(provider.getLogoPath(), "w92"),
                        providersData.getLink()
                ));
            }
        }
        
        // 구매 (buy)
        if (providersData.getBuy() != null) {
            for (TmdbApiClient.TmdbProvider provider : providersData.getBuy()) {
                result.add(new OttDto(
                        provider.getProviderId().longValue(),
                        provider.getProviderName(),
                        "BUY",
                        "KR",
                        tmdbApiClient.getImageUrl(provider.getLogoPath(), "w92"),
                        providersData.getLink()
                ));
            }
        }
        
        return result;
    }
    
    /**
     * TMDB의 OTT 제공 정보를 DB에 저장 (MovieService에서 사용)
     */
    private void saveOttProvidersToDb(Long movieId, TmdbApiClient.TmdbWatchProvidersData providersData, String region) {
        Movie movie = repo.findById(movieId)
                .orElseThrow(() -> new RuntimeException("영화를 찾을 수 없음: ID=" + movieId));
        
        String linkUrl = providersData.getLink();
        
        // 구독 서비스 (flatrate)
        if (providersData.getFlatrate() != null) {
            for (TmdbApiClient.TmdbProvider provider : providersData.getFlatrate()) {
                saveOttProvider(movie, provider, OttProvider.Type.SUBSCRIPTION, region, linkUrl);
            }
        }
        
        // 대여 서비스 (rent)
        if (providersData.getRent() != null) {
            for (TmdbApiClient.TmdbProvider provider : providersData.getRent()) {
                saveOttProvider(movie, provider, OttProvider.Type.RENT, region, linkUrl);
            }
        }
        
        // 구매 서비스 (buy)
        if (providersData.getBuy() != null) {
            for (TmdbApiClient.TmdbProvider provider : providersData.getBuy()) {
                saveOttProvider(movie, provider, OttProvider.Type.BUY, region, linkUrl);
            }
        }
    }
    
    /**
     * 개별 OTT 제공 정보 저장
     */
    private void saveOttProvider(Movie movie, TmdbApiClient.TmdbProvider tmdbProvider, 
                                 OttProvider.Type type, String region, String linkUrl) {
        try {
            // OTT Provider 찾기 또는 생성
            String providerName = tmdbProvider.getProviderName();
            if (providerName == null || providerName.isEmpty()) {
                return; // 이름이 없으면 건너뛰기
            }
            
            // Provider가 이미 존재하는지 확인
            java.util.Optional<OttProvider> existingProviderOpt = ottProviderRepository.findByName(providerName);
            final OttProvider provider;
            
            if (existingProviderOpt.isPresent()) {
                provider = existingProviderOpt.get();
            } else {
                // 새 Provider 생성 (리플렉션 사용)
                OttProvider newProvider = new OttProvider();
                try {
                    java.lang.reflect.Field nameField = OttProvider.class.getDeclaredField("name");
                    nameField.setAccessible(true);
                    nameField.set(newProvider, providerName);
                    
                    java.lang.reflect.Field typeField = OttProvider.class.getDeclaredField("type");
                    typeField.setAccessible(true);
                    typeField.set(newProvider, type);
                    
                    if (tmdbProvider.getLogoPath() != null) {
                        java.lang.reflect.Field logoField = OttProvider.class.getDeclaredField("logoUrl");
                        logoField.setAccessible(true);
                        logoField.set(newProvider, tmdbApiClient.getImageUrl(tmdbProvider.getLogoPath(), "w92"));
                    }
                } catch (Exception e) {
                    log.warn("⚠️ OTT Provider 생성 실패 (리플렉션): {}", e.getMessage());
                    return;
                }
                provider = ottProviderRepository.save(newProvider);
            }
            
            entityManager.flush();
            
            // MovieOtt 관계 확인 (중복 방지)
            final Long providerId = provider.getId();
            List<MovieOtt> existingMappings = movieOttRepository.findByMovieAndRegion(movie, region);
            boolean exists = existingMappings.stream()
                    .anyMatch(mo -> mo.getProvider().getId().equals(providerId));
            
            if (!exists) {
                // MovieOtt 생성 (리플렉션 사용)
                MovieOtt movieOtt = new MovieOtt();
                try {
                    java.lang.reflect.Field movieField = MovieOtt.class.getDeclaredField("movie");
                    movieField.setAccessible(true);
                    movieField.set(movieOtt, movie);
                    
                    java.lang.reflect.Field providerField = MovieOtt.class.getDeclaredField("provider");
                    providerField.setAccessible(true);
                    providerField.set(movieOtt, provider);
                    
                    java.lang.reflect.Field regionField = MovieOtt.class.getDeclaredField("region");
                    regionField.setAccessible(true);
                    regionField.set(movieOtt, region);
                    
                    if (linkUrl != null) {
                        java.lang.reflect.Field linkField = MovieOtt.class.getDeclaredField("linkUrl");
                        linkField.setAccessible(true);
                        linkField.set(movieOtt, linkUrl);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ MovieOtt 생성 실패 (리플렉션): {}", e.getMessage());
                    return;
                }
                movieOttRepository.save(movieOtt);
                entityManager.flush();
            }
        } catch (Exception e) {
            // 개별 OTT 제공 정보 저장 실패는 로그만 남기고 계속 진행
            log.warn("⚠️ OTT 제공 정보 저장 실패 (무시): movieId={}, providerName={}, error={}", 
                    movie.getId(), tmdbProvider.getProviderName(), e.getMessage());
        }
    }
}
