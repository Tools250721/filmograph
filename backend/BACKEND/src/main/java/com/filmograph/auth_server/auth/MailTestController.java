package com.filmograph.auth_server.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class MailTestController {
    private final EmailService emailService;

    public MailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/send")
    public Map<String, Object> sendMail(
            @RequestParam(required = false, defaultValue = "sryang24@gmail.com") String to) {
        try {
            emailService.sendMail(to, "테스트 메일", "이 메일은 스프링부트에서 보낸 테스트 메일입니다.");
            return Map.of("ok", true, "message", "메일 전송 완료!", "to", to);
        } catch (org.springframework.mail.MailSendException e) {
            return Map.of("ok", false,
                    "error", "메일 전송 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"),
                    "to", to,
                    "hint", "이메일 주소 형식을 확인하거나 SMTP 설정을 확인하세요.");
        } catch (Exception e) {
            return Map.of("ok", false,
                    "error", "메일 전송 실패: " + (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"),
                    "type", e.getClass().getSimpleName(),
                    "to", to);
        }
    }
}
