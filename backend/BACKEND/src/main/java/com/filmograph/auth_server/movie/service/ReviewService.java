package com.filmograph.auth_server.movie.service;

import com.filmograph.auth_server.movie.dto.MovieResponseDto;
import com.filmograph.auth_server.movie.domain.Review;
import com.filmograph.auth_server.movie.repo.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    //  특정 사용자가 평가한 영화 목록 조회
    public List<MovieResponseDto> getRatedMoviesByUser(Long userId) {
        List<Review> reviews = reviewRepository.findAllByUserId(userId);

        return reviews.stream()
                .filter(r -> r.getMovie() != null) // 영화 없는 리뷰 방지
                .map(r -> MovieResponseDto.builder()
                        .id(r.getMovie().getId())
                        .title(r.getMovie().getTitle())
                        .posterUrl(r.getMovie().getPosterUrl())
                        .releaseDate(r.getMovie().getReleaseDate())
                        .director(r.getMovie().getDirector())
                        .build()
                ).collect(Collectors.toList());
    }
}
