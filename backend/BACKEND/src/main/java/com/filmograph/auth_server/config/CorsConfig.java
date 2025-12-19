package com.filmograph.auth_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 전역 CORS 및 정적 리소스 설정
 * - 프론트엔드(React/Vite 등)와 백엔드가 다른 포트일 때 API 호출 허용
 * - 업로드된 이미지 파일 서빙
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 모든 API 경로 허용
                        .allowedOrigins(
                                "http://localhost:3000", // React 기본 포트
                                "http://localhost:5173" // Vite 기본 포트
                )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*") // 모든 헤더 허용
                        .allowCredentials(true) // 쿠키/인증정보 포함 허용
                        .maxAge(3600); // Preflight 요청 캐시 (1시간)
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // 업로드된 이미지 파일 서빙
                String uploadPath = Paths.get("uploads").toAbsolutePath().toString();
                registry.addResourceHandler("/static/**")
                        .addResourceLocations("file:" + uploadPath + "/");
            }
        };
    }
}
