package com.HelloMate.HelloMateBackend.domain.auth.entity;

import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원가입/비밀번호 찾기/마이페이지 비밀번호 변경이 공유하는 "이메일로 코드 발송 → 확인" 컴포넌트.
 */
@Entity
@Table(name = "email_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailVerificationPurpose purpose;

    @Column(nullable = false)
    private boolean used;

    @Column(length = 255)
    private String resetToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private int attemptCount;

    public EmailVerification(String id, String email, String code, EmailVerificationPurpose purpose,
                              LocalDateTime expiresAt) {
        this.id = id;
        this.email = email;
        this.code = code;
        this.purpose = purpose;
        this.used = false;
        this.expiresAt = expiresAt;
        this.attemptCount = 0;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matches(String candidateCode) {
        return this.code.equals(candidateCode);
    }

    public void markUsed(String resetToken) {
        this.used = true;
        this.resetToken = resetToken;
    }

    public void invalidateResetToken() {
        this.resetToken = null;
    }

    public void increaseAttemptCount() {
        this.attemptCount++;
    }

    public boolean isAttemptExceeded(int maxAttempt) {
        return this.attemptCount >= maxAttempt;
    }
}
