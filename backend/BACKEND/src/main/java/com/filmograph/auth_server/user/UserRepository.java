package com.filmograph.auth_server.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 사용자 관련 DB 접근을 처리하는 Repository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일 대소문자 무시하고 사용자 조회 (로그인, 비밀번호 재설정, 인증용)
    Optional<User> findByEmailIgnoreCase(String email);

    // 이메일 대소문자 무시하고 존재 여부 확인 (회원가입 중복 방지용)
    boolean existsByEmailIgnoreCase(String email);

    // 이메일 대소문자 구분해서 조회 (필요 시 사용)
    Optional<User> findByEmail(String email);

    // 이메일 대소문자 구분해서 존재 여부 확인 (기존 코드 보존)
    boolean existsByEmail(String email);
}
