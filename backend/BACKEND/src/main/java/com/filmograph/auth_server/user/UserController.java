package com.filmograph.auth_server.user;

import com.filmograph.auth_server.auth.SecurityUserHelper;
import com.filmograph.auth_server.user.dto.UpdateNameRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final SecurityUserHelper userHelper;

    @Autowired
    public UserController(UserService userService, SecurityUserHelper userHelper) {
        this.userService = userService;
        this.userHelper = userHelper;
    }

    // ✅ 내 프로필 정보 조회
    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getMyProfile(HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            return ResponseEntity.ok(Map.of(
                    "id", me.getId(),
                    "name", me.getName() != null ? me.getName() : "",
                    "email", me.getEmail() != null ? me.getEmail() : "",
                    "profileImageUrl", me.getProfileImageUrl() != null ? me.getProfileImageUrl() : "",
                    "backgroundImageUrl", me.getBackgroundImageUrl() != null ? me.getBackgroundImageUrl() : ""));
        } catch (Exception e) {
            throw e;
        }
    }

    // ✅ 이름 수정
    @PutMapping("/me/name")
    public ResponseEntity<?> updateName(@RequestBody UpdateNameRequest request,
            HttpServletRequest req) {
        // 현재 로그인한 사용자 가져오기 (JWT 기반)
        User me = userHelper.requireCurrentUser(req);

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("이름은 비워둘 수 없습니다.");
        }

        User updated = userService.updateName(me.getId(), request.name());
        return ResponseEntity.ok(Map.of(
                "id", updated.getId(),
                "name", updated.getName()));
    }

    // ✅ 이미지 업로드 (본인 계정만 가능)
    @PostMapping("/me/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam String type,
            @RequestParam MultipartFile file,
            HttpServletRequest req) {
        try {
            User me = userHelper.requireCurrentUser(req);
            String imageUrl = userService.uploadImage(me.getId(), type, file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // ✅ 회원 탈퇴 (본인 계정만 가능)
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(HttpServletRequest req) {
        User me = userHelper.requireCurrentUser(req);
        userService.deleteUser(me.getId());
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
