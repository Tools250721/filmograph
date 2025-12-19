package com.filmograph.auth_server.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // 1. CORS 설정 통합
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 기본 허용 경로
                        .requestMatchers("/error", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/static/**").permitAll()

                        // 인증(로그인/회원가입) 관련
                        .requestMatchers("/api/auth/**").permitAll()

                        // 공통 조회 API (로그인 없이 가능)
                        .requestMatchers("/api/ranking/**", "/api/v1/rankings/**").permitAll()
                        .requestMatchers("/boxoffice/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/movies/**", "/api/movies/**", "/api/v1/movies/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/ratings/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies/*/ratings").permitAll()
                        .requestMatchers("/api/v1/quotes/**", "/api/v1/recommendations/**").permitAll()
                        .requestMatchers("/api/v1/admin/movies/**", "/api/test/**").permitAll()


                        // 그 외 모든 요청(POST, PUT, DELETE 등)은 인증 필요
                        .anyRequest().authenticated())

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.err.println("=== 인증 실패 (403 Forbidden) ===");
                            System.err.println("Path: " + request.getRequestURI());
                            System.err.println("Method: " + request.getMethod());
                            System.err.println("Authorization Header: " + request.getHeader("Authorization"));

                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            try {
                                response.getWriter().write(
                                        "{\"ok\":false,\"error\":\"인증이 필요하거나 토큰이 유효하지 않습니다.\",\"path\":\""
                                                + request.getRequestURI() + "\"}");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }))

                // JWT 필터 추가
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 2. CORS 상세 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 주소 모두 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}