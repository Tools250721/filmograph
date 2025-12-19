package com.filmograph.auth_server.auth.comment;

import com.filmograph.auth_server.auth.SecurityUserHelper;
import com.filmograph.auth_server.auth.dto.CommentResponse;
import com.filmograph.auth_server.auth.dto.CommentRequest;
import com.filmograph.auth_server.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final SecurityUserHelper securityUserHelper;

    @PostMapping("/{movieId}")
    public ResponseEntity<?> writeComment(
            @PathVariable Long movieId,
            @RequestBody CommentRequest dto,
            HttpServletRequest req
    ) {
        User user = securityUserHelper.requireCurrentUser(req);
        commentService.writeComment(user.getId(), movieId, dto);
        return ResponseEntity.ok("댓글 작성 완료!");
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<CommentResponse>> getCommentsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CommentResponse> comments = commentService.getCommentsByMovie(movieId, sort, page, size);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/my")
    public ResponseEntity<List<CommentResponse>> getMyComments(
            @RequestParam(defaultValue = "latest") String sort,
            HttpServletRequest req
    ) {
        User user = securityUserHelper.requireCurrentUser(req);
        List<CommentResponse> myComments = commentService.getMyComments(user.getId(), sort);
        return ResponseEntity.ok(myComments);
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long commentId,
            HttpServletRequest req
    ) {
        User user = securityUserHelper.requireCurrentUser(req);
        commentService.toggleLike(commentId, user.getId());
        return ResponseEntity.ok("좋아요 상태 변경 완료!");
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest dto,
            HttpServletRequest req
    ) {
        User user = securityUserHelper.requireCurrentUser(req);
        commentService.updateComment(commentId, user.getId(), dto);
        return ResponseEntity.ok("댓글 수정 완료!");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            HttpServletRequest req
    ) {
        User user = securityUserHelper.requireCurrentUser(req);
        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.ok("댓글 삭제 완료!");
    }
}

