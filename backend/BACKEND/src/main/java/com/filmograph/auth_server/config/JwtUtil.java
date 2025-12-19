package com.filmograph.auth_server.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;
    private final String issuer;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-minutes}") long accessMinutes,
            @Value("${app.jwt.refresh-days}") long refreshDays,
            @Value("${app.jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessMinutes * 60 * 1000L;
        this.refreshTokenValidityMs = refreshDays * 24 * 60 * 60 * 1000L;
        this.issuer = issuer;
    }

    public String createAccessToken(String email) {
        return createAccessToken(email, Map.of());
    }

    public String createAccessToken(String email, Map<String, Object> extraClaims) {
        return buildTokenWithSubject(email, extraClaims, accessTokenValidityMs);
    }

    public String createRefreshToken(String email) {
        return buildTokenWithSubject(email, Map.of("type", "refresh"), refreshTokenValidityMs);
    }

    public String createToken(Map<String, Object> claims, long ttlSeconds) {
        long validityMs = ttlSeconds * 1000L;
        String subject = String.valueOf(claims.getOrDefault("email", ""));
        return buildTokenRaw(subject, claims, validityMs);
    }

    public String createAccessToken(Map<String, Object> claims) {
        String subject = String.valueOf(claims.getOrDefault("email", ""));
        return buildTokenRaw(subject, claims, accessTokenValidityMs);
    }

    public String createRefreshToken(Map<String, Object> claims) {
        String subject = String.valueOf(claims.getOrDefault("email", ""));
        return buildTokenRaw(subject, claims, refreshTokenValidityMs);
    }

    private String buildTokenWithSubject(String subjectEmail, Map<String, Object> claims, long validityMs) {
        return buildTokenRaw(subjectEmail, claims, validityMs);
    }

    private String buildTokenRaw(String subject, Map<String, Object> claims, long validityMs) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + validityMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getAllClaims(token).getSubject();
    }

    public Date getExpiration(String token) {
        return getAllClaims(token).getExpiration();
    }

    @SuppressWarnings("unchecked")
    public <T> T getClaim(String token, String keyName, Class<T> clazz) {
        Object v = getAllClaims(token).get(keyName);
        return v == null ? null : (T) v;
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            getAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("❌ JWT 토큰 검증 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("   원인: " + e.getCause().getMessage());
            }
            return false;
        }
    }
}
