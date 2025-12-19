package com.filmograph.auth_server.movie.domain;

import com.filmograph.auth_server.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 유저 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 영화 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /** 사용자가 매긴 평점 (nullable) */
    @Column(nullable = true)
    private Double rating;

    /** 유저 영화 상태 (예: WATCHED, WISHLIST, WATCHING, FAVORITE 등) */
    @Enumerated(EnumType.STRING)   // ✅ Enum을 문자열로 저장
    @Column(nullable = false, length = 20)  // 충분한 길이 보장 (가장 긴 값: WISHLIST = 8자)
    private UserMovieStatus status;

    /** 생성일시 */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /** 수정일시 */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /** 평점 수정 헬퍼 */
    public void updateRating(Double rating) {
        this.rating = rating;
    }

    /** 상태 수정 헬퍼 */
    public void updateStatus(UserMovieStatus status) {
        this.status = status;
    }
}
