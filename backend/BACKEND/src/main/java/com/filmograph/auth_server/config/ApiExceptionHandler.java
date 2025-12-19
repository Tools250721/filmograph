package com.filmograph.auth_server.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import io.jsonwebtoken.JwtException;

import java.util.Map;

/**
 * 전역 예외 처리 클래스
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** 잘못된 요청 (입력값 오류 등) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "error", e.getMessage()));
    }

    /** JWT 예외 */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> unauthorized(JwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("ok", false, "error", "invalid or expired token"));
    }

    /** 인증/인가 예외 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> illegalState(IllegalStateException e) {
        String message = e.getMessage();
        if (message != null && (message.contains("토큰") || message.contains("Authorization") || message.contains("헤더"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "error", message));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "error", message != null ? message : "잘못된 요청입니다."));
    }
    
    /** Spring Security 인증 예외 */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> accessDenied(org.springframework.security.access.AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("ok", false, "error", "접근이 거부되었습니다. 인증이 필요합니다."));
    }
    
    /** Spring Security 인증 예외 */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<?> authenticationException(org.springframework.security.core.AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("ok", false, "error", "인증이 필요합니다. JWT 토큰을 확인해주세요."));
    }

    /** Multipart 파싱 예외 (GET 요청에 multipart가 포함된 경우 등) */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> multipartException(MultipartException e) {
        String message = e.getMessage();
        // GET 요청에서 multipart 에러가 발생한 경우
        if (message != null && message.contains("parse multipart")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("ok", false, "error", "GET 요청에는 파일 업로드를 포함할 수 없습니다. Body를 비워주세요."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "error", message != null ? message : "파일 업로드 처리 중 오류가 발생했습니다."));
    }

    /** JSON 파싱 예외 */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<?> httpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException e) {
        System.err.println("=== JSON 파싱 오류 ===");
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, "error", "잘못된 JSON 형식입니다: " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류")));
    }
    
    /** HTTP 메서드가 지원되지 않는 경우 */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> methodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException e) {
        System.err.println("=== HTTP 메서드가 지원되지 않음 ===");
        System.err.println("Error: " + e.getMessage());
        String supportedMethods = e.getSupportedMethods() != null ? String.join(", ", e.getSupportedMethods()) : "알 수 없음";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("ok", false, 
                        "error", e.getMessage() != null ? e.getMessage() : "지원되지 않는 HTTP 메서드입니다.",
                        "supportedMethods", supportedMethods));
    }
    
    /** 메일 전송 예외 */
    @ExceptionHandler(org.springframework.mail.MailSendException.class)
    public ResponseEntity<?> mailSendException(org.springframework.mail.MailSendException e) {
        System.err.println("=== 메일 전송 실패 (MailSendException) ===");
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("ok", false, 
                        "error", "메일 전송 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"),
                        "hint", "이메일 주소 형식을 확인하거나 SMTP 설정을 확인하세요."));
    }
    
    /** 메서드 인자 타입 불일치 예외 (예: URL 파라미터 타입 오류) */
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> methodArgumentTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
        System.err.println("=== 메서드 인자 타입 불일치 (MethodArgumentTypeMismatchException) ===");
        System.err.println("Parameter Name: " + e.getName());
        System.err.println("Required Type: " + (e.getRequiredType() != null ? e.getRequiredType().getName() : "null"));
        System.err.println("Actual Value: " + e.getValue());
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        
        String hint = "URL 파라미터를 확인하세요. 예: /api/v1/quotes/movie/57 (57은 영화 ID)";
        if (e.getName() != null && e.getName().contains("movieId")) {
            hint = "movieId는 숫자여야 합니다. 예: /api/v1/quotes/movie/57";
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, 
                        "error", "잘못된 파라미터 형식: " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"),
                        "parameter", e.getName() != null ? e.getName() : "unknown",
                        "requiredType", e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown",
                        "actualValue", e.getValue() != null ? e.getValue().toString() : "null",
                        "hint", hint));
    }

    /** 트랜잭션 롤백 예외 */
    @ExceptionHandler(org.springframework.transaction.UnexpectedRollbackException.class)
    public ResponseEntity<?> unexpectedRollback(org.springframework.transaction.UnexpectedRollbackException e) {
        System.err.println("=== 트랜잭션 롤백 예외 (UnexpectedRollbackException) ===");
        System.err.println("Error: " + e.getMessage());
        if (e.getCause() != null) {
            System.err.println("Cause: " + e.getCause().getMessage());
            System.err.println("Cause Class: " + e.getCause().getClass().getName());
            if (e.getCause().getCause() != null) {
                System.err.println("Root Cause: " + e.getCause().getCause().getMessage());
                System.err.println("Root Cause Class: " + e.getCause().getCause().getClass().getName());
            }
        }
        e.printStackTrace();
        
        String message = e.getMessage();
        String rootCauseMsg = "";
        if (e.getCause() != null && e.getCause().getCause() != null) {
            rootCauseMsg = e.getCause().getCause().getMessage();
        } else if (e.getCause() != null) {
            rootCauseMsg = e.getCause().getMessage();
        }
        
        // Data truncation 에러인 경우
        if (rootCauseMsg != null && (rootCauseMsg.contains("Data truncation") || rootCauseMsg.contains("too long"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, 
                            "error", "데이터베이스 컬럼 길이 제한 초과: " + rootCauseMsg,
                            "hint", "데이터베이스 스키마를 수정해야 합니다. fix_movie_schema.sql을 실행하세요."));
        }
        
        if (message != null && message.contains("rollback-only")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, 
                            "error", "데이터 저장 중 오류가 발생했습니다. 입력값을 확인해주세요.",
                            "hint", "중복된 데이터이거나 필수 필드가 누락되었을 수 있습니다.",
                            "details", rootCauseMsg != null ? rootCauseMsg : message));
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("ok", false, 
                        "error", "트랜잭션 오류: " + (message != null ? message : "알 수 없는 오류"),
                        "details", rootCauseMsg != null ? rootCauseMsg : ""));
    }
    
    /** 데이터베이스 제약 조건 위반 */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<?> dataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException e) {
        System.err.println("=== 데이터베이스 제약 조건 위반 (DataIntegrityViolationException) ===");
        System.err.println("Error: " + e.getMessage());
        if (e.getRootCause() != null) {
            System.err.println("Root Cause: " + e.getRootCause().getMessage());
        }
        e.printStackTrace();
        
        String rootCauseMsg = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
        if (rootCauseMsg != null && (rootCauseMsg.contains("duplicate") || rootCauseMsg.contains("UNIQUE"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("ok", false, "error", "이미 존재하는 항목입니다."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("ok", false, 
                        "error", "데이터베이스 제약 조건 위반",
                        "details", rootCauseMsg != null ? rootCauseMsg : e.getMessage()));
    }
    
    /** not found 류 메시지 통일 처리 (선택) */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> runtime(RuntimeException e) {
        System.err.println("=== 전역 예외 핸들러 (RuntimeException) ===");
        System.err.println("Exception Type: " + e.getClass().getName());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
        
        String message = e.getMessage();
        if (message != null && message.toLowerCase().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("ok", false, "error", message));
        }
        // 개발 환경에서는 상세 에러 메시지 표시
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("ok", false, 
                        "error", message != null ? message : "internal server error",
                        "type", e.getClass().getSimpleName()));
    }
    
    /** 모든 예외를 잡는 최종 핸들러 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception e) {
        System.err.println("=== 전역 예외 핸들러 (Exception) ===");
        System.err.println("Exception Type: " + e.getClass().getName());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("ok", false, 
                        "error", e.getMessage() != null ? e.getMessage() : "서버 오류가 발생했습니다.",
                        "type", e.getClass().getSimpleName()));
    }
}
