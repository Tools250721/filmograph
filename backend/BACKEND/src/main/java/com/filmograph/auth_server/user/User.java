package com.filmograph.auth_server.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.filmograph.auth_server.movie.domain.UserMovie;
import com.filmograph.auth_server.auth.dto.PasswordResetToken;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "ux_users_email", columnList = "email", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "background_image_url", length = 255)
    private String backgroundImageUrl;

    // 새로 추가된 부분 — 유저 역할(role)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String role = "ROLE_USER"; // 기본값 일반 사용자

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* ===== 연관 관계 ===== */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserMovie> userMovies = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PasswordResetToken> resetTokens = new ArrayList<>();

    /* ===== 편의 메서드 ===== */
    public String getEmailNormalized() {
        return email == null ? null : email.trim();
    }

    public void setPasswordHash(String hash) {
        this.passwordHash = (hash == null ? "" : hash);
    }

    //  CustomUserDetailsService에서 사용하는 getter
    public String getRole() {
        return role;
    }

    /* ===== UserDetails 구현 메서드 ===== */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // role 필드 기반으로 Spring Security 권한 설정
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
