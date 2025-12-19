package com.filmograph.auth_server.auth;

import com.filmograph.auth_server.config.JwtUtil;
import com.filmograph.auth_server.user.User;
import com.filmograph.auth_server.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JWT 토큰에서 이메일을 추출하거나,
 * 이메일 기준으로 UserDetails 를 불러오는 헬퍼
 */
@Component
public class SecurityUserHelper implements UserDetailsService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public SecurityUserHelper(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    /** Spring Security 가 사용할 수 있는 UserDetails 로 변환 */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    /** 토큰 문자열을 직접 받아서 사용자 찾기 */
    public User requireCurrentUser(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new IllegalStateException("유효하지 않은 토큰입니다");
        }

        String email = jwtUtil.getEmailFromToken(token);
        if (email == null) {
            throw new IllegalStateException("토큰에서 이메일을 찾을 수 없습니다");
        }

        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + email));
    }

    /** HttpServletRequest 에서 Authorization 헤더를 읽어 사용자 찾기 */
    public User requireCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Authorization 헤더가 필요합니다 (Bearer ...)");
        }

        String token = authHeader.substring(7);
        return requireCurrentUser(token); // 기존 메서드 재사용
    }

    /** HttpServletRequest에서 로그인한 사용자의 ID만 가져오기 */
    public Long getCurrentUserId(HttpServletRequest request) {
        User currentUser = requireCurrentUser(request);
        return currentUser.getId();
    }

    /** HttpServletRequest에서 현재 사용자를 Optional로 반환 (인증이 없어도 예외 발생하지 않음) */
    public Optional<User> getCurrentUser(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Optional.empty();
            }

            String token = authHeader.substring(7);
            if (token == null || !jwtUtil.validateToken(token)) {
                return Optional.empty();
            }

            String email = jwtUtil.getEmailFromToken(token);
            if (email == null) {
                return Optional.empty();
            }

            return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
