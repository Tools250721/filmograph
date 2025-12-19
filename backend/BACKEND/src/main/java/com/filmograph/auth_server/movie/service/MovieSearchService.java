package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.dto.MovieResponseDto;
import com.filmograph.auth_server.movie.dto.MovieSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieSearchService {

    private final KmdbService kmdbService;

    /**
     * KMDB 영화 검색 (제목 / 배우 / 연도 지원)
     */
    public Page<MovieResponseDto> search(MovieSearchRequest req) {
        int page = Math.max(req.getPage(), 0);
        int size = Math.max(req.getSize(), 10);

        List<MovieResponseDto> result = kmdbService.searchMovies(req.getKeyword(), req.getYear());

        return new PageImpl<>(
                result,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "releaseDate")),
                result.size()
        );
    }

    /**
     * 테스트용 로컬 검색
     */
    public Page<MovieResponseDto> searchLocal(MovieSearchRequest req) {
        List<MovieResponseDto> result = List.of(
                MovieResponseDto.builder()
                        .id(1L).title("Inception").director("Christopher Nolan").releaseDate("2010").nation("USA")
                        .build(),
                MovieResponseDto.builder()
                        .id(2L).title("Interstellar").director("Christopher Nolan").releaseDate("2014").nation("USA")
                        .build()
        );
        return new PageImpl<>(result, PageRequest.of(req.getPage(), req.getSize()), result.size());
    }

    /**
     * 단건 조회 (제목으로)
     */
    public MovieResponseDto importOneByTitle(String title) {
        return kmdbService.findOneByTitle(title);
    }
}

