package com.filmograph.auth_server.auth;

import com.filmograph.auth_server.auth.dto.*;
import com.filmograph.auth_server.config.JwtUtil;
import com.filmograph.auth_server.user.User;
import com.filmograph.auth_server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${filmograph.reset-password-url:http://localhost:5173/reset-password}")
    private String resetPasswordUrl;

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        final String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        User user = User.builder()
                .name(req.name())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .build();
        userRepository.save(user);
        return new RegisterResponse(user.getId(), user.getName(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        final String email = req.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        String accessToken = jwtUtil.createToken(
                Map.of("id", user.getId(), "email", user.getEmail()),
                30 * 60
        );
        String refreshToken = jwtUtil.createToken(
                Map.of("id", user.getId(), "email", user.getEmail(), "type", "refresh"),
                14 * 24 * 60 * 60
        );
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public void sendResetLink(String emailRaw) {
        final String email = emailRaw.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
        
        // 기존 토큰이 있으면 삭제 (한 사용자당 하나의 활성 토큰만 유지)
        List<PasswordResetToken> existingTokens = tokenRepository.findByUser(user);
        tokenRepository.deleteAll(existingTokens);
        
        // 새 토큰 생성 (24시간 유효)
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);
        
        PasswordResetToken token = new PasswordResetToken(resetToken, user, expiryDate);
        tokenRepository.save(token);
        
        // 이메일 전송
        String resetLink = resetPasswordUrl + "?token=" + resetToken;
        String emailSubject = "비밀번호 재설정 요청";
        String emailText = String.format(
            "안녕하세요 %s님,\n\n" +
            "비밀번호 재설정을 요청하셨습니다.\n\n" +
            "아래 링크를 클릭하여 비밀번호를 재설정하세요:\n" +
            "%s\n\n" +
            "이 링크는 24시간 동안 유효합니다.\n\n" +
            "만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시하셔도 됩니다.\n\n" +
            "감사합니다.",
            user.getName(),
            resetLink
        );
        
        try {
            emailService.sendMail(user.getEmail(), emailSubject, emailText);
        } catch (Exception e) {
            // 이메일 전송 실패 시 토큰 삭제
            tokenRepository.delete(token);
            System.err.println("이메일 전송 실패: " + e.getMessage());
            throw new RuntimeException("이메일 전송에 실패했습니다. 나중에 다시 시도해주세요.", e);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // 토큰으로 사용자 찾기
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));
        
        // 토큰 만료 확인
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("토큰이 만료되었습니다. 비밀번호 재설정을 다시 요청해주세요.");
        }
        
        // 비밀번호 변경
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // 사용된 토큰 삭제
        tokenRepository.delete(resetToken);
    }
}

