package com.filmograph.auth_server.auth;

import com.filmograph.auth_server.auth.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest req) {
        RegisterResponse response = service.register(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        LoginResponse response = service.login(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-request")
    public ResponseEntity<Map<String, String>> resetRequest(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        service.sendResetLink(email);
        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 링크를 이메일로 전송했습니다."));
    }

    @PostMapping("/reset/confirm")
    public ResponseEntity<Map<String, String>> confirmReset(@RequestBody ConfirmResetPasswordRequest req) {
        service.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }
}

