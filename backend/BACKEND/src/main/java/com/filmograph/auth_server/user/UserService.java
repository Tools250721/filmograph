package com.filmograph.auth_server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    
    @Value("${filmograph.upload.base-dir:uploads}")
    private String uploadBaseDir;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 이메일로 사용자 찾기
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    //  이름 수정
    @Transactional
    public User updateName(Long userId, String newName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));
        user.setName(newName);
        return userRepository.save(user);
    }

    //  이미지 업로드
    @Transactional
    public String uploadImage(Long userId, String type, MultipartFile file) {
        log.info("이미지 업로드 시작 - userId: {}, type: {}, file: {}", userId, type, 
                file != null ? file.getOriginalFilename() : "null");
        
        if (file == null || file.isEmpty()) {
            log.error("파일이 null이거나 비어있습니다. file: {}", file);
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        
        if (!type.equals("profile") && !type.equals("background")) {
            log.error("잘못된 type: {}", type);
            throw new IllegalArgumentException("type은 'profile' 또는 'background'여야 합니다.");
        }
        
        try {
            String folder = type.equals("profile") ? "profile" : "background";
            
            // 파일명 정리 (한글 및 특수문자 처리)
            String originalFilename = file.getOriginalFilename();
            log.info("원본 파일명: {}, 파일 크기: {} bytes", originalFilename, file.getSize());
            
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "image";
            }
            // 확장자 추출
            String extension = "";
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0) {
                extension = originalFilename.substring(lastDot);
            }
            
            // UUID로 안전한 파일명 생성
            String fileName = UUID.randomUUID().toString() + extension;
            log.info("생성된 파일명: {}", fileName);
            
            // 업로드 디렉토리 생성 (프로젝트 루트 기준)
            // 프로젝트 루트 디렉토리 찾기
            String projectRoot = System.getProperty("user.dir");
            log.info("현재 작업 디렉토리: {}", projectRoot);
            
            // 프로젝트 구조에 따라 BACKEND 폴더 찾기
            // 1. 현재 디렉토리가 BACKEND인 경우
            if (projectRoot.endsWith("BACKEND")) {
                // 그대로 사용
            }
            // 2. 현재 디렉토리가 프로젝트 루트인 경우
            else if (projectRoot.endsWith("tools backend 2025")) {
                projectRoot = projectRoot + File.separator + "BACKEND";
                log.info("BACKEND 폴더로 경로 조정: {}", projectRoot);
            }
            // 3. 그 외의 경우 (임시 디렉토리 등) - BACKEND 폴더 찾기
            else {
                // 상위 디렉토리들을 확인하여 BACKEND 폴더 찾기
                File currentDir = new File(projectRoot);
                File backendDir = findBackendDirectory(currentDir);
                if (backendDir != null && backendDir.exists()) {
                    projectRoot = backendDir.getAbsolutePath();
                    log.info("BACKEND 폴더 찾음: {}", projectRoot);
                } else {
                    // 찾지 못하면 현재 디렉토리에 uploads 폴더 생성
                    log.warn("BACKEND 폴더를 찾을 수 없어 현재 디렉토리 사용: {}", projectRoot);
                }
            }
            
            // 업로드 디렉토리 경로 구성
            String uploadDir = projectRoot + File.separator + uploadBaseDir + File.separator + folder;
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            log.info("최종 업로드 경로: {}", uploadPath);
            
            // 디렉토리가 없으면 생성 (부모 디렉토리까지 모두 생성)
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성 완료: {}", uploadPath.toAbsolutePath());
            }
            
            // 파일 저장 (절대 경로 사용)
            Path filePath = uploadPath.resolve(fileName).toAbsolutePath();
            log.info("파일 저장 시도 (절대 경로): {}", filePath);
            
            // 파일 저장
            file.transferTo(filePath.toFile());
            log.info("파일 저장 완료: {}", filePath);
            
            // 파일이 실제로 저장되었는지 확인
            if (!Files.exists(filePath)) {
                log.error("파일 저장 후 확인 실패: {}", filePath.toAbsolutePath());
                throw new RuntimeException("파일 저장에 실패했습니다.");
            }
            log.info("파일 존재 확인 완료, 크기: {} bytes", Files.size(filePath));

            // 사용자 정보 업데이트
            log.info("사용자 정보 업데이트 시작 - userId: {}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없습니다: {}", userId);
                        return new RuntimeException("사용자를 찾을 수 없습니다: " + userId);
                    });

            String imageUrl = "/static/" + folder + "/" + fileName;
            if (type.equals("profile")) {
                user.setProfileImageUrl(imageUrl);
                log.info("프로필 이미지 URL 설정: {}", imageUrl);
            } else {
                user.setBackgroundImageUrl(imageUrl);
                log.info("배경 이미지 URL 설정: {}", imageUrl);
            }

            userRepository.save(user);
            log.info("사용자 정보 저장 완료");

            return imageUrl;
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("이미지 업로드 실패 - userId: {}, type: {}, error: {}", userId, type, e.getMessage(), e);
            throw new RuntimeException("이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    //  회원 탈퇴 (연관 데이터 포함 삭제)
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다."));
        userRepository.delete(user); // Cascade로 UserMovie, PasswordResetToken도 같이 삭제됨
    }
    
    /**
     * BACKEND 디렉토리를 찾는 헬퍼 메서드
     */
    private File findBackendDirectory(File startDir) {
        File current = startDir;
        int maxDepth = 5; // 최대 5단계 상위 디렉토리까지 검색
        
        for (int i = 0; i < maxDepth; i++) {
            // 현재 디렉토리가 BACKEND인지 확인
            if (current.getName().equals("BACKEND") && current.isDirectory()) {
                return current;
            }
            
            // 상위 디렉토리로 이동
            File parent = current.getParentFile();
            if (parent == null) {
                break;
            }
            current = parent;
        }
        
        return null;
    }
}
