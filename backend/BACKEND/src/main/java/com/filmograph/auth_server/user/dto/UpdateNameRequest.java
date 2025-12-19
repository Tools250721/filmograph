package com.filmograph.auth_server.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 마이페이지 이름 변경 요청 DTO
 */
public record UpdateNameRequest(

        @NotBlank(message = "이름은 비워둘 수 없습니다.")
        @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
        String name

) { }
