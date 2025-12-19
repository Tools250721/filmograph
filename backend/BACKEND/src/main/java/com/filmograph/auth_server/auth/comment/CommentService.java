package com.filmograph.auth_server.auth.comment;

import com.filmograph.auth_server.auth.dto.CommentRequest;
import com.filmograph.auth_server.auth.dto.CommentResponse;
import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.repo.MovieRepository;
import com.filmograph.auth_server.user.User;
import com.filmograph.auth_server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @Transactional
    public Comment writeComment(Long userId, Long movieId, CommentRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Comment comment = Comment.builder()
                .writer(user)
                .movie(movie)
                .content(dto.getContent())
                .rating(dto.getRating())
                .createdAt(LocalDateTime.now())
                .likeCount(0)
                .build();

        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByMovie(Long movieId, String sort, int page, int size) {
        Sort sortOption = switch (sort.toLowerCase()) {
            case "high" -> Sort.by(Sort.Order.desc("likeCount"));
            case "low" -> Sort.by(Sort.Order.asc("likeCount"));
            case "oldest" -> Sort.by(Sort.Order.asc("createdAt"));
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };

        Pageable pageable = PageRequest.of(page, size, sortOption);
        Page<Comment> commentPage = commentRepository.findByMovieId(movieId, pageable);

        return commentPage.map(c -> {
            if (c.getWriter() != null) {
                Hibernate.initialize(c.getWriter());
            }
            
            return CommentResponse.builder()
                    .id(c.getId())
                    .content(c.getContent())
                    .rating(c.getRating())
                    .likeCount(c.getLikeCount())
                    .createdAt(c.getCreatedAt())
                    .userId(c.getWriter() != null ? c.getWriter().getId() : null)
                    .nickname(c.getWriter() != null ? c.getWriter().getName() : null)
                    .profileImage(c.getWriter() != null ? c.getWriter().getProfileImageUrl() : null)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getMyComments(Long userId, String sort) {
        List<Comment> comments = commentRepository.findByWriter_Id(userId);

        List<CommentResponse> responses = comments.stream()
                .map(c -> {
                    if (c.getWriter() != null) {
                        Hibernate.initialize(c.getWriter());
                    }
                    
                    return CommentResponse.builder()
                            .id(c.getId())
                            .content(c.getContent())
                            .rating(c.getRating())
                            .likeCount(c.getLikeCount())
                            .createdAt(c.getCreatedAt())
                            .userId(c.getWriter() != null ? c.getWriter().getId() : null)
                            .nickname(c.getWriter() != null ? c.getWriter().getName() : null)
                            .profileImage(c.getWriter() != null ? c.getWriter().getProfileImageUrl() : null)
                            .build();
                })
                .toList();

        return switch (sort.toLowerCase()) {
            case "high" -> responses.stream()
                    .sorted((a, b) -> Long.compare(b.getLikeCount(), a.getLikeCount()))
                    .toList();
            case "low" -> responses.stream()
                    .sorted((a, b) -> Long.compare(a.getLikeCount(), b.getLikeCount()))
                    .toList();
            case "oldest" -> responses.stream()
                    .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .toList();
            default -> responses.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();
        };
    }

    @Transactional
    public void toggleLike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentAndUser(comment, user);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        } else {
            CommentLike like = CommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            commentLikeRepository.save(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }

        commentRepository.save(comment);
    }

    @Transactional
    public void updateComment(Long commentId, Long userId, CommentRequest dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("본인 댓글만 수정할 수 있습니다.");
        }

        comment.setContent(dto.getContent());
        comment.setRating(dto.getRating());
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getWriter().getId().equals(userId)) {
            throw new IllegalStateException("본인 댓글만 삭제할 수 있습니다.");
        }

        List<CommentLike> likes = commentLikeRepository.findAll()
                .stream()
                .filter(l -> l.getComment().getId().equals(commentId))
                .toList();

        commentLikeRepository.deleteAll(likes);
        commentRepository.delete(comment);
    }
}

