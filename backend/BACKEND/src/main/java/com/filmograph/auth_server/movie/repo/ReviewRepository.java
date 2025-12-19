package com.filmograph.auth_server.movie.repo;

import com.filmograph.auth_server.movie.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 유저가 작성한 모든 리뷰 조회
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId")
    List<Review> findAllByUserId(@Param("userId") Long userId);
}
