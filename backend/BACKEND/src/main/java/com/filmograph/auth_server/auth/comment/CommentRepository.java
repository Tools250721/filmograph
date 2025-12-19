package com.filmograph.auth_server.auth.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByMovieIdOrderByIdDesc(Long movieId);
    List<Comment> findByMovieId(Long movieId);
    List<Comment> findByWriterId(Long writerId);

    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.writer WHERE c.movie.id = :movieId", 
           countQuery = "SELECT count(c) FROM Comment c WHERE c.movie.id = :movieId")
    Page<Comment> findByMovieId(@Param("movieId") Long movieId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c JOIN FETCH c.writer WHERE c.writer.id = :userId")
    List<Comment> findByWriter_Id(@Param("userId") Long userId);
}

