package com.filmograph.auth_server.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filmograph.auth_server.movie.dto.BoxOfficeDto;
import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.repo.MovieRepository;
import com.filmograph.auth_server.movie.repo.MovieSearchRepository;
import com.filmograph.auth_server.movie.external.TmdbApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * KOBIS 박스오피스 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoxOfficeService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MovieRepository movieRepository;
    private final MovieSearchRepository movieSearchRepository;
    private final TmdbApiClient tmdbApiClient;

    @Value("${app.api.kobis.key}")
    private String KOBIS_KEY;

    /**
     * 일일 박스오피스 Top10 조회
     */
    public List<BoxOfficeDto> getDailyBoxOfficeTop10() {
        // 어제 날짜 기준
        String targetDt = LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String url = "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json"
                + "?key=" + KOBIS_KEY
                + "&targetDt=" + targetDt;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        List<BoxOfficeDto> result = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode list = root.path("boxOfficeResult").path("dailyBoxOfficeList");

            for (JsonNode item : list) {
                String movieNm = item.path("movieNm").asText();

                // 영화 제목으로 DB에서 찾기 (유연한 매칭)
                Long movieId = null;
                String posterUrl = null;

                // 1. 정확한 제목 매칭 시도
                Optional<Movie> movieOpt = movieRepository.findByTitle(movieNm);

                // 2. 정확한 매칭이 없으면 부분 일치 검색 시도
                if (movieOpt.isEmpty() && movieNm != null && !movieNm.trim().isEmpty()) {
                    Page<Movie> searchResults = movieSearchRepository.search(
                            movieNm, (Integer) null, (Integer) null,
                            PageRequest.of(0, 5));
                    if (searchResults.hasContent()) {
                        // 첫 번째 결과 사용
                        movieOpt = Optional.of(searchResults.getContent().get(0));
                    }
                }

                // 3. 여전히 없으면 제목에서 괄호/부제 제거 후 재시도
                if (movieOpt.isEmpty() && movieNm != null) {
                    String cleanedTitle = movieNm
                            .replaceAll("\\(.*?\\)", "") // 괄호 내용 제거
                            .replaceAll("\\s+", " ") // 연속 공백 정리
                            .trim();
                    if (!cleanedTitle.isEmpty() && !cleanedTitle.equals(movieNm)) {
                        movieOpt = movieRepository.findByTitle(cleanedTitle);
                        if (movieOpt.isEmpty()) {
                            Page<Movie> searchResults = movieSearchRepository.search(
                                    cleanedTitle, (Integer) null, (Integer) null,
                                    PageRequest.of(0, 5));
                            if (searchResults.hasContent()) {
                                movieOpt = Optional.of(searchResults.getContent().get(0));
                            }
                        }
                    }
                }

                if (movieOpt.isPresent()) {
                    var movie = movieOpt.get();
                    movieId = movie.getId();
                    posterUrl = movie.getPosterUrl();
                }

                // 포스터가 없으면 TMDB에서 검색 시도
                if ((posterUrl == null || posterUrl.trim().isEmpty()) && movieNm != null && !movieNm.trim().isEmpty()) {
                    try {
                        log.info("박스오피스 포스터 없음, TMDB 검색 시도: {}", movieNm);
                        TmdbApiClient.TmdbSearchResponse tmdbSearch = tmdbApiClient.searchMovies(movieNm, 1);
                        if (tmdbSearch != null && tmdbSearch.getResults() != null
                                && !tmdbSearch.getResults().isEmpty()) {
                            TmdbApiClient.TmdbMovie tmdbMovie = tmdbSearch.getResults().get(0);
                            if (tmdbMovie.getPosterPath() != null && !tmdbMovie.getPosterPath().trim().isEmpty()) {
                                posterUrl = "https://image.tmdb.org/t/p/w500" + tmdbMovie.getPosterPath();
                                log.info("TMDB에서 포스터 찾음: {}", posterUrl);

                                // 로컬 DB에도 포스터 업데이트 (선택적)
                                if (movieOpt.isPresent()) {
                                    var movie = movieOpt.get();
                                    if (movie.getPosterUrl() == null || movie.getPosterUrl().trim().isEmpty()) {
                                        movie.setPosterUrl(posterUrl);
                                        movieRepository.save(movie);
                                        log.info("로컬 DB 포스터 업데이트 완료: movieId={}", movie.getId());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("TMDB 포스터 검색 실패 (무시됨): {} - {}", movieNm, e.getMessage());
                    }
                }

                BoxOfficeDto dto = BoxOfficeDto.builder()
                        .rank(item.path("rank").asInt())
                        .movieNm(movieNm)
                        .openDt(item.path("openDt").asText())
                        .salesAcc(item.path("salesAcc").asText())
                        .audiAcc(item.path("audiAcc").asText())
                        .movieId(movieId)
                        .posterUrl(posterUrl)
                        .build();
                result.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("박스오피스 API 파싱 중 오류 발생", e);
        }
        return result;
    }
}
