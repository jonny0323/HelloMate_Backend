package com.HelloMate.HelloMateBackend.domain.auth.service;

import com.HelloMate.HelloMateBackend.domain.auth.entity.EmailVerification;
import com.HelloMate.HelloMateBackend.domain.auth.entity.EmailVerificationPurpose;
import com.HelloMate.HelloMateBackend.domain.auth.repository.EmailVerificationRepository;
import com.HelloMate.HelloMateBackend.domain.email.service.EmailService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 회원가입 이메일 인증 / 비밀번호 찾기 / 마이페이지 비밀번호 변경이 공유하는
 * "이메일로 6자리 코드 발송 → 확인" 로직 (설계: docs/roadmaps/password-reset-email-verification.md).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    /** 디자인의 인증번호 타이머가 03:00에서 시작한다. */
    private static final int CODE_VALIDITY_MINUTES = 3;

    /** 6자리(10^6)는 무제한 재시도를 허용하면 뚫린다. */
    private static final int MAX_ATTEMPT = 5;

    /** [인증 번호 재발송] 연타로 메일 발송량이 튀는 걸 막는다. */
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private static final List<String> ALLOWED_SCHOOL_EMAIL_SUFFIXES = List.of(".ac.kr", ".edu");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;

    @Transactional
    public void sendCode(String email, EmailVerificationPurpose purpose) {
        if (purpose == EmailVerificationPurpose.SIGNUP && !isSchoolEmail(email)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "학교 공식 이메일만 사용할 수 있습니다.");
        }
        requireResendCooldownPassed(email, purpose);

        String code = generateCode();
        EmailVerification verification = new EmailVerification(
                UuidCreator.create(), email, code, purpose, LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES));
        emailVerificationRepository.save(verification);
        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public EmailVerification confirmCode(String email, String code, EmailVerificationPurpose purpose) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE));

        if (verification.isUsed()) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        if (verification.isExpired()) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (verification.isAttemptExceeded(MAX_ATTEMPT)) {
            throw new BusinessException(ErrorCode.VERIFICATION_ATTEMPT_EXCEEDED);
        }

        // 시도 횟수는 성공/실패와 무관하게 먼저 올린다 — 실패 경로에서 예외로 빠져나가도 카운트가 남아야 한다.
        verification.increaseAttemptCount();
        if (!verification.matches(code)) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        String resetToken = purpose == EmailVerificationPurpose.PASSWORD_RESET ? UuidCreator.create() : null;
        verification.markUsed(resetToken);
        return verification;
    }

    @Transactional
    public EmailVerification consumeResetToken(String resetToken) {
        EmailVerification verification = emailVerificationRepository
                .findByResetTokenAndPurpose(resetToken, EmailVerificationPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESET_TOKEN));
        if (verification.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_RESET_TOKEN);
        }
        verification.invalidateResetToken();
        return verification;
    }

    private void requireResendCooldownPassed(String email, EmailVerificationPurpose purpose) {
        emailVerificationRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .filter(latest -> latest.getCreatedAt() != null)
                .filter(latest -> latest.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(LocalDateTime.now()))
                .ifPresent(latest -> {
                    throw new BusinessException(ErrorCode.VERIFICATION_RESEND_TOO_SOON);
                });
    }

    private boolean isSchoolEmail(String email) {
        String lower = email.toLowerCase();
        return ALLOWED_SCHOOL_EMAIL_SUFFIXES.stream().anyMatch(lower::endsWith);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
