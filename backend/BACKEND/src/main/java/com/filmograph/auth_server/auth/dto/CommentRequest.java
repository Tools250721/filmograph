package com.filmograph.auth_server.auth.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {
    private Long movieId;   // 어떤 영화에 다는지
    private Long userId;    // 누가 작성하는지
    private String content; // 내용
    private Double rating;
}
