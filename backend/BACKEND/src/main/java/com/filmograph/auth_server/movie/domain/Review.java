package com.filmograph.auth_server.movie.domain;

import com.filmograph.auth_server.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리뷰 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // User 엔티티가 이미 있다면 이걸로 연결

    // 어떤 영화에 대한 리뷰인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    // 평점 (예: 1~5점)
    private Double rating;

    // 리뷰 내용
    @Column(length = 1000)
    private String comment;
}
