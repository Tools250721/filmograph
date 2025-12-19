package com.filmograph.auth_server.auth.comment;

import com.filmograph.auth_server.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);
    long countByComment(Comment comment);
    boolean existsByCommentAndUser(Comment comment, User user);
}

