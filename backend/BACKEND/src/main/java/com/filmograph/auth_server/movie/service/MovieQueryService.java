package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.dto.MyRatedMovieDto;
import com.filmograph.auth_server.movie.external.TmdbApiClient;
import com.filmograph.auth_server.movie.repo.MovieSearchRepository;
import com.filmograph.auth_server.movie.repo.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieQueryService {

    private final MovieSearchRepository repo;
    private final RatingRepository ratingRepository;
    private final MovieExternalService movieExternalService;
    private final TmdbApiClient tmdbApiClient;

    public Page<Movie> search(
            String q,
            Long genreId,              // 현재 스펙에선 사용하지 않음(무시)
            Integer yearFrom,
            Integer yearTo,
            String sort,
            String order,
            int page,
            int size
    ) {
        // 정렬 컬럼 매핑 (엔티티 실제 필드명 기준)
        String sortProp = switch (sort == null ? "id" : sort) {
            case "title" -> "title";
            case "year"  -> "releaseYear";
            default -> "id";
        };
        Sort.Direction dir = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortProp));

        // 먼저 DB에서 검색
        Page<Movie> dbResults = repo.search(q, yearFrom, yearTo, pageable);
        
        // 검색어가 있고, DB 결과가 없거나 적으면 (10개 미만) TMDB에서도 검색
        // 첫 페이지에서만 TMDB 검색 수행 (성능 고려)
        if (q != null && !q.trim().isEmpty() && dbResults.getTotalElements() < 10 && page == 0) {
            try {
                log.info("DB 검색 결과가 적어서 TMDB에서도 검색: query={}, dbResults={}", q, dbResults.getTotalElements());
                
                // TMDB에서 검색 (트랜잭션 외부에서 실행)
                // 첫 페이지와 두 번째 페이지를 모두 가져와서 더 많은 결과 확인
                TmdbApiClient.TmdbSearchResponse tmdbResponse1 = tmdbApiClient.searchMovies(q, 1);
                TmdbApiClient.TmdbSearchResponse tmdbResponse2 = null;
                
                // 첫 페이지 결과가 있으면 두 번째 페이지도 가져오기
                if (tmdbResponse1 != null && tmdbResponse1.getResults() != null && !tmdbResponse1.getResults().isEmpty()) {
                    if (tmdbResponse1.getTotalPages() > 1) {
                        tmdbResponse2 = tmdbApiClient.searchMovies(q, 2);
                    }
                }
                
                // DB에 이미 있는 영화 제목들 (중복 방지)
                Set<String> existingTitles = dbResults.getContent().stream()
                        .map(Movie::getTitle)
                        .filter(title -> title != null)
                        .collect(Collectors.toSet());
                
                // DB에 이미 있는 영화의 TMDB ID도 확인 (더 정확한 중복 방지)
                Set<Long> existingTmdbIds = dbResults.getContent().stream()
                        .map(Movie::getTmdbId)
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());
                
                // TMDB 결과를 합치기
                List<TmdbApiClient.TmdbMovie> allTmdbMovies = new ArrayList<>();
                if (tmdbResponse1 != null && tmdbResponse1.getResults() != null) {
                    allTmdbMovies.addAll(tmdbResponse1.getResults());
                }
                if (tmdbResponse2 != null && tmdbResponse2.getResults() != null) {
                    allTmdbMovies.addAll(tmdbResponse2.getResults());
                }
                
                if (!allTmdbMovies.isEmpty()) {
                    log.info("TMDB 검색 결과: {}개 영화 발견", allTmdbMovies.size());
                    
                    // TMDB 결과 중 최대 10개까지 가져와서 저장 (별도 트랜잭션으로 처리)
                    List<Movie> newMovies = new ArrayList<>();
                    int count = 0;
                    for (TmdbApiClient.TmdbMovie tmdbMovie : allTmdbMovies) {
                        if (count >= 10) break;
                        
                        // 이미 DB에 있는 영화는 건너뛰기 (제목 또는 TMDB ID로 확인)
                        if (existingTitles.contains(tmdbMovie.getTitle()) || 
                            existingTmdbIds.contains(tmdbMovie.getId())) {
                            log.debug("이미 DB에 있는 영화 건너뛰기: title={}, tmdbId={}", tmdbMovie.getTitle(), tmdbMovie.getId());
                            continue;
                        }
                        
                        try {
                            // 상세 정보 조회 후 저장 (별도 트랜잭션으로 실행)
                            TmdbApiClient.TmdbMovieDetail detail = tmdbApiClient.getMovieDetail(tmdbMovie.getId());
                            // saveMovieFromTmdb는 이미 @Transactional이 있으므로 별도 트랜잭션으로 실행됨
                            Movie saved = saveMovieInNewTransaction(detail);
                            newMovies.add(saved);
                            existingTitles.add(saved.getTitle());
                            if (saved.getTmdbId() != null) {
                                existingTmdbIds.add(saved.getTmdbId());
                            }
                            count++;
                            log.info("TMDB에서 영화 저장 성공: title={}, id={}, tmdbId={}", 
                                    saved.getTitle(), saved.getId(), saved.getTmdbId());
                        } catch (Exception e) {
                            log.warn("TMDB 영화 저장 실패: title={}, tmdbId={}, error={}", 
                                    tmdbMovie.getTitle(), tmdbMovie.getId(), e.getMessage(), e);
                            // 개별 영화 저장 실패는 무시하고 계속 진행
                        }
                    }
                    
                    if (!newMovies.isEmpty()) {
                        log.info("TMDB에서 {}개 영화를 가져와서 저장 완료, 재검색 수행", newMovies.size());
                        // DB 결과와 새로 저장한 영화를 합쳐서 다시 검색
                        return repo.search(q, yearFrom, yearTo, pageable);
                    } else {
                        log.info("TMDB 검색 결과 중 새로 저장할 영화가 없음 (모두 이미 DB에 있음)");
                    }
                } else {
                    log.info("TMDB 검색 결과가 없음: query={}", q);
                }
            } catch (Exception e) {
                log.error("TMDB 검색 중 오류 발생: query={}, error={}", q, e.getMessage(), e);
                // TMDB 검색 실패해도 DB 결과는 반환
            }
        }
        
        return dbResults;
    }
    
    /**
     * 별도 트랜잭션으로 영화 저장 (트랜잭션 롤백 방지)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Movie saveMovieInNewTransaction(TmdbApiClient.TmdbMovieDetail detail) {
        return movieExternalService.saveMovieFromTmdb(detail);
    }

    // ====== [신규] 내가 별점 남긴 영화들 정렬 ======
    // sort: rating_desc | rating_asc | rated_desc | liked_first(옵션)
    public Page<MyRatedMovieDto> getMyRatedMovies(Long userId, String sort, int page, int size) {
        if (sort == null || sort.isBlank()) sort = "rated_desc";
        Pageable pageable = PageRequest.of(page, size); // 정렬은 JPQL ORDER BY에서 처리

        return switch (sort) {
            case "rating_desc" -> ratingRepository.findMyMoviesOrderByRatingDesc(userId, pageable);
            case "rating_asc"  -> ratingRepository.findMyMoviesOrderByRatingAsc(userId, pageable);
            case "liked_first" -> ratingRepository.findMyMoviesLikedFirst(userId, pageable); // 옵션
            case "rated_desc"  -> ratingRepository.findMyMoviesOrderByRatedAt(userId, pageable);
            default            -> ratingRepository.findMyMoviesOrderByRatedAt(userId, pageable);
        };
    }
}
