package com.filmograph.auth_server.auth.dto;

import com.filmograph.auth_server.auth.comment.Comment;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private Double rating;
    private long likeCount;
    private Long userId;
    private String nickname;
    private String profileImage;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(String.valueOf(comment.getWriter())) // User 이름 표시
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
