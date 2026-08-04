package com.HelloMate.HelloMateBackend.domain.email.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StubEmailService implements EmailService {

    @Override
    public void sendVerificationCode(String to, String code) {
        log.info("[StubEmailService] {} 로 인증번호 {} 발송(스텁 — 실제 발송 없음)", to, code);
    }
}
