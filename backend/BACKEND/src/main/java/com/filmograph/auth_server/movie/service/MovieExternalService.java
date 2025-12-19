package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.Genre;
import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.domain.MovieOtt;
import com.filmograph.auth_server.movie.domain.OttProvider;
import com.filmograph.auth_server.movie.external.TmdbApiClient;
import com.filmograph.auth_server.movie.repo.GenreRepository;
import com.filmograph.auth_server.movie.repo.MovieRepository;
import com.filmograph.auth_server.movie.repo.MovieOttRepository;
import com.filmograph.auth_server.movie.repo.OttProviderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 외부 API(TMDB)에서 영화 데이터를 가져와서 데이터베이스에 저장하는 서비스
 */
@Service
@RequiredArgsConstructor
public class MovieExternalService {

    private final TmdbApiClient tmdbApiClient;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final OttProviderRepository ottProviderRepository;
    private final MovieOttRepository movieOttRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Repository 접근을 위한 getter (테스트 및 디버깅용)
    public MovieRepository getMovieRepository() {
        return movieRepository;
    }

    /**
     * TMDB에서 영화를 검색하고 데이터베이스에 저장
     */
    @Transactional(rollbackFor = Exception.class)
    public Movie searchAndSaveMovie(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("검색어가 비어있습니다.");
            }

            String trimmedQuery = query.trim();
            TmdbApiClient.TmdbSearchResponse response = tmdbApiClient.searchMovies(trimmedQuery, 1);

            if (response == null) {
                throw new RuntimeException("TMDB API 호출 실패: 응답이 없습니다. 검색어를 확인해주세요: " + trimmedQuery);
            }

            if (response.getResults() == null || response.getResults().isEmpty()) {
                throw new RuntimeException("영화를 찾을 수 없습니다: '" + trimmedQuery + "'. 다른 검색어를 시도해보세요.");
            }

            TmdbApiClient.TmdbMovie tmdbMovie = response.getResults().get(0);
            TmdbApiClient.TmdbMovieDetail detail = tmdbApiClient.getMovieDetail(tmdbMovie.getId());
            Movie saved = saveMovieFromTmdb(detail);

            return saved;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("영화 가져오기 실패: " + e.getMessage(), e);
        }
    }

    /**
     * TMDB 영화 상세 정보를 데이터베이스에 저장
     */
    @Transactional(rollbackFor = Exception.class)
    public Movie saveMovieFromTmdb(TmdbApiClient.TmdbMovieDetail tmdbMovie) {
        try {
            if (tmdbMovie.getId() != null) {
                Optional<Movie> existingByTmdbId = movieRepository.findByTmdbId(tmdbMovie.getId());
                if (existingByTmdbId.isPresent()) {
                    return existingByTmdbId.get();
                }
            }

            if (tmdbMovie.getTitle() != null && !tmdbMovie.getTitle().trim().isEmpty()) {
                Optional<Movie> existing = movieRepository.findByTitle(tmdbMovie.getTitle());
                if (existing.isPresent()) {
                    if (existing.get().getTmdbId() == null && tmdbMovie.getId() != null) {
                        existing.get().setTmdbId(tmdbMovie.getId());
                        movieRepository.save(existing.get());
                    }
                    return existing.get();
                }
            }

            String overview = tmdbMovie.getOverview();

            // 영화 생성
            Movie movie = Movie.builder()
                    .tmdbId(tmdbMovie.getId())
                    .title(tmdbMovie.getTitle())
                    .originalTitle(tmdbMovie.getOriginalTitle())
                    .overview(overview)
                    .runtimeMinutes(tmdbMovie.getRuntime())
                    .posterUrl(tmdbApiClient.getImageUrl(tmdbMovie.getPosterPath(), "w500"))
                    .backdropUrl(tmdbApiClient.getImageUrl(tmdbMovie.getBackdropPath(), "original"))
                    .build();

            // 개봉일 파싱
            if (tmdbMovie.getReleaseDate() != null && !tmdbMovie.getReleaseDate().isEmpty()) {
                try {
                    LocalDate releaseDate = LocalDate.parse(tmdbMovie.getReleaseDate(), DateTimeFormatter.ISO_DATE);
                    movie.setReleaseYear(releaseDate.getYear());
                    movie.setReleaseDate(tmdbMovie.getReleaseDate());
                } catch (Exception e) {
                    // 파싱 실패 시 무시
                }
            }

            // 제작 국가
            if (tmdbMovie.getProductionCountries() != null && !tmdbMovie.getProductionCountries().isEmpty()) {
                String country = tmdbMovie.getProductionCountries().get(0).get("iso_3166_1");
                movie.setCountry(country);
            }

            // 감독 정보 저장 (crew에서 Director 찾기)
            if (tmdbMovie.getCredits() != null && tmdbMovie.getCredits().getCrew() != null) {
                String directorName = tmdbMovie.getCredits().getCrew().stream()
                        .filter(person -> "Directing".equals(person.getKnownForDepartment()) ||
                                "Director".equals(person.getJob()))
                        .findFirst()
                        .map(TmdbApiClient.TmdbPerson::getName)
                        .orElse(null);
                movie.setDirector(directorName);
            }

            Movie savedMovie = movieRepository.save(movie);
            entityManager.flush();
            Long movieId = savedMovie.getId();

            // 장르 매핑 (실패해도 영화는 저장되도록 try-catch로 감쌈)
            if (tmdbMovie.getGenres() != null && !tmdbMovie.getGenres().isEmpty()) {
                try {
                    // 장르 목록 준비
                    List<Genre> genres = tmdbMovie.getGenres().stream()
                            .map(tmdbGenre -> {
                                // 장르가 없으면 생성
                                Genre genre = genreRepository.findByName(tmdbGenre.getName())
                                        .orElseGet(() -> {
                                            Genre newGenre = new Genre();
                                            newGenre.setName(tmdbGenre.getName());
                                            return genreRepository.save(newGenre);
                                        });
                                entityManager.flush();
                                return genre;
                            })
                            .collect(Collectors.toList());

                    // 직접 SQL로 movie_genre 테이블에 삽입
                    for (Genre genre : genres) {
                        try {
                            // 기존 관계가 있는지 확인
                            Query checkQuery = entityManager.createNativeQuery(
                                    "SELECT COUNT(*) FROM movie_genre WHERE movie_id = ? AND genre_id = ?");
                            checkQuery.setParameter(1, movieId);
                            checkQuery.setParameter(2, genre.getId());
                            Long count = ((Number) checkQuery.getSingleResult()).longValue();

                            // 관계가 없으면 삽입
                            if (count == 0) {
                                Query insertQuery = entityManager.createNativeQuery(
                                        "INSERT INTO movie_genre (movie_id, genre_id) VALUES (?, ?)");
                                insertQuery.setParameter(1, movieId);
                                insertQuery.setParameter(2, genre.getId());
                                insertQuery.executeUpdate();
                            }
                        } catch (Exception e) {
                            // 개별 장르 삽입 실패는 무시
                        }
                    }
                    entityManager.flush();
                } catch (Exception e) {
                    // 장르 저장 실패해도 영화는 저장됨
                }
            }

            if (tmdbMovie.getWatchProviders() != null && tmdbMovie.getWatchProviders().getResults() != null) {
                try {
                    Map<String, TmdbApiClient.TmdbWatchProvidersData> results = tmdbMovie.getWatchProviders()
                            .getResults();
                    String[] regions = { "KR", "US" };
                    for (String region : regions) {
                        TmdbApiClient.TmdbWatchProvidersData providersData = results.get(region);
                        if (providersData != null) {
                            saveOttProviders(movieId, providersData, region);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // OTT 정보 저장 실패해도 영화는 저장됨
                }
            }

            Movie managedMovie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new RuntimeException("영화 저장 후 조회 실패: ID=" + movieId));

            return managedMovie;

        } catch (org.springframework.dao.DataIntegrityViolationException e) {

            String rootCauseMsg = e.getRootCause() != null ? e.getRootCause().getMessage() : "";
            if (rootCauseMsg.contains("duplicate") || rootCauseMsg.contains("Duplicate entry") ||
                    rootCauseMsg.contains("UNIQUE") || rootCauseMsg.contains("unique constraint")) {
                if (tmdbMovie.getId() != null) {
                    Optional<Movie> existing = movieRepository.findByTmdbId(tmdbMovie.getId());
                    if (existing.isPresent()) {
                        return existing.get();
                    }
                }

                if (tmdbMovie.getTitle() != null && !tmdbMovie.getTitle().trim().isEmpty()) {
                    Optional<Movie> existing = movieRepository.findByTitle(tmdbMovie.getTitle());
                    if (existing.isPresent()) {
                        return existing.get();
                    }
                }

                // 찾지 못한 경우 예외 전파
                throw new RuntimeException("영화가 이미 존재하지만 조회에 실패했습니다. 제목: " + tmdbMovie.getTitle(), e);
            }

            // 다른 종류의 DataIntegrityViolationException은 그대로 전파
            throw new RuntimeException("데이터베이스 제약 조건 위반: " + rootCauseMsg, e);
        } catch (Exception e) {
            throw new RuntimeException("영화 저장 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"), e);
        }
    }

    /**
     * TMDB의 OTT 제공 정보를 DB에 저장
     */
    private void saveOttProviders(Long movieId, TmdbApiClient.TmdbWatchProvidersData providersData, String region) {
        Movie movie = movieRepository.findById(movieId)
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
            Optional<OttProvider> existingProviderOpt = ottProviderRepository.findByName(providerName);
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
                    return;
                }
                movieOttRepository.save(movieOtt);
                entityManager.flush();
            }
        } catch (Exception e) {
            // 개별 OTT 제공 정보 저장 실패는 무시
        }
    }

    /**
     * 모든 영화 삭제 (관련 데이터 포함)
     * 외래키 제약조건을 고려하여 순서대로 삭제
     */
    @Transactional
    public int deleteAllMovies() {
        List<Movie> allMovies = movieRepository.findAll();
        int count = allMovies.size();

        // 영화와 관련된 모든 데이터를 삭제
        // JPA의 Cascade 설정에 따라 자동으로 삭제되지만, 명시적으로 처리
        for (Movie movie : allMovies) {
            // 연관 관계 제거
            movie.getActors().clear();
            movie.getGenres().clear();
        }

        // 모든 영화 삭제
        movieRepository.deleteAll();

        return count;
    }

    /**
     * TMDB 인기 영화 목록 가져오기
     */
    public List<TmdbApiClient.TmdbMovie> getPopularMovies(int page) {
        TmdbApiClient.TmdbSearchResponse response = tmdbApiClient.getPopularMovies(page);
        return response != null ? response.getResults() : List.of();
    }

    /**
     * TMDB 인기 영화를 여러 페이지 가져와서 DB에 저장
     * 
     * @param pages 저장할 페이지 수 (예: 5 = 5페이지 = 최대 100개 영화)
     * @return 저장된 영화 수
     */
    public int importPopularMovies(int pages) {
        int savedCount = 0;
        for (int page = 1; page <= pages; page++) {
            try {
                TmdbApiClient.TmdbSearchResponse response = tmdbApiClient.getPopularMovies(page);
                if (response == null || response.getResults() == null) {
                    continue;
                }

                for (TmdbApiClient.TmdbMovie tmdbMovie : response.getResults()) {
                    try {
                        // 상세 정보 조회
                        TmdbApiClient.TmdbMovieDetail detail = tmdbApiClient.getMovieDetail(tmdbMovie.getId());
                        if (detail != null) {
                            // 저장 전에 이미 존재하는지 확인
                            boolean alreadyExists = false;
                            if (detail.getId() != null) {
                                alreadyExists = movieRepository.findByTmdbId(detail.getId()).isPresent();
                            }
                            if (!alreadyExists) {
                                alreadyExists = movieRepository.findByTitle(detail.getTitle()).isPresent();
                            }

                            saveMovieFromTmdb(detail);
                            if (!alreadyExists) {
                                savedCount++;
                            }
                        }
                    } catch (Exception e) {
                        // 개별 영화 저장 실패는 무시하고 계속 진행
                    }
                }
            } catch (Exception e) {
                // 페이지 처리 실패는 무시하고 계속 진행
            }
        }
        return savedCount;
    }

    /**
     * TMDB 트렌딩 영화를 가져와서 DB에 저장
     * 
     * @param timeWindow "day" 또는 "week"
     * @param pages      저장할 페이지 수
     * @return 저장된 영화 수
     */
    public int importTrendingMovies(String timeWindow, int pages) {
        int savedCount = 0;
        for (int page = 1; page <= pages; page++) {
            try {
                TmdbApiClient.TmdbSearchResponse response = tmdbApiClient.getTrendingMovies(timeWindow, page);
                if (response == null || response.getResults() == null) {
                    continue;
                }

                for (TmdbApiClient.TmdbMovie tmdbMovie : response.getResults()) {
                    try {
                        // 저장 전에 이미 존재하는지 확인
                        boolean alreadyExists = false;
                        if (tmdbMovie.getId() != null) {
                            alreadyExists = movieRepository.findByTmdbId(tmdbMovie.getId()).isPresent();
                        }
                        if (!alreadyExists) {
                            alreadyExists = movieRepository.findByTitle(tmdbMovie.getTitle()).isPresent();
                        }

                        TmdbApiClient.TmdbMovieDetail detail = tmdbApiClient.getMovieDetail(tmdbMovie.getId());
                        if (detail != null) {
                            saveMovieFromTmdb(detail);
                            if (!alreadyExists) {
                                savedCount++;
                            }
                        }
                    } catch (Exception e) {
                        // 개별 영화 저장 실패는 무시하고 계속 진행
                    }
                }
            } catch (Exception e) {
                // 페이지 처리 실패는 무시하고 계속 진행
            }
        }
        return savedCount;
    }

    /**
     * TMDB Top Rated 영화를 가져와서 DB에 저장
     * 
     * @param pages 저장할 페이지 수
     * @return 저장된 영화 수
     */
    public int importTopRatedMovies(int pages) {
        int savedCount = 0;
        for (int page = 1; page <= pages; page++) {
            try {
                TmdbApiClient.TmdbSearchResponse response = tmdbApiClient.getTopRatedMovies(page);
                if (response == null || response.getResults() == null) {
                    continue;
                }

                for (TmdbApiClient.TmdbMovie tmdbMovie : response.getResults()) {
                    try {
                        // 저장 전에 이미 존재하는지 확인
                        boolean alreadyExists = false;
                        if (tmdbMovie.getId() != null) {
                            alreadyExists = movieRepository.findByTmdbId(tmdbMovie.getId()).isPresent();
                        }
                        if (!alreadyExists) {
                            alreadyExists = movieRepository.findByTitle(tmdbMovie.getTitle()).isPresent();
                        }

                        TmdbApiClient.TmdbMovieDetail detail = tmdbApiClient.getMovieDetail(tmdbMovie.getId());
                        if (detail != null) {
                            saveMovieFromTmdb(detail);
                            if (!alreadyExists) {
                                savedCount++;
                            }
                        }
                    } catch (Exception e) {
                        // 개별 영화 저장 실패는 무시하고 계속 진행
                    }
                }
            } catch (Exception e) {
                // 페이지 처리 실패는 무시하고 계속 진행
            }
        }
        return savedCount;
    }

    /**
     * TMDB 현재 상영 중인 영화를 가져와서 DB에 저장
     * 
     * @param pages 저장할 페이지 수
     * @return 저장된 영화 수
     */
    public int importNowPlayingMovies(int pages) {
        int savedCount = 0;
        for (int page = 1; page <= pages; page++) {
            try {
                TmdbApiClient.TmdbSearchResponse response = tmdbApiClient.getNowPlayingMovies(page);
                if (response == null || response.getResults() == null) {
                    continue;
                }

                for (TmdbApiClient.TmdbMovie tmdbMovie : response.getResults()) {
                    try {
                        // 저장 전에 이미 존재하는지 확인 (saveMovieFromTmdb 내부에서도 확인하지만, 여기서 미리 확인하여 로깅)
                        boolean alreadyExists = false;
                        if (tmdbMovie.getId() != null) {
                            alreadyExists = movieRepository.findByTmdbId(tmdbMovie.getId()).isPresent();
                        }
                        if (!alreadyExists && tmdbMovie.getTitle() != null) {
                            alreadyExists = movieRepository.findByTitle(tmdbMovie.getTitle()).isPresent();
                        }

                        TmdbApiClient.TmdbMovieDetail detail = tmdbApiClient.getMovieDetail(tmdbMovie.getId());
                        if (detail != null) {
                            Long beforeCount = movieRepository.count();
                            saveMovieFromTmdb(detail);
                            Long afterCount = movieRepository.count();

                            boolean wasNewlySaved = afterCount > beforeCount;
                            if (wasNewlySaved) {
                                savedCount++;
                            }
                        }
                    } catch (Exception e) {
                        // 개별 영화 저장 실패는 무시하고 계속 진행
                    }
                }
            } catch (Exception e) {
                // 페이지 처리 실패는 무시하고 계속 진행
            }
        }
        return savedCount;
    }
}
