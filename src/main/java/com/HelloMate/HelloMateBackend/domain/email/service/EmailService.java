package com.HelloMate.HelloMateBackend.domain.email.service;

/**
 * 실제 서비스에서는 SES/SMTP 등으로 메일을 발송하는 구현체로 교체한다.
 * 지금은 외부 인프라가 없어 로그만 남기는 스텁 구현체({@link StubEmailService})만 제공한다.
 */
public interface EmailService {

    void sendVerificationCode(String to, String code);
}
