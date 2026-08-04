package com.HelloMate.HelloMateBackend.domain.auth.service;

import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StudentLoginRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StudentSignUpRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.PasswordResetVerifyResponse;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.StudentSignUpResponse;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.TokenResponse;
import com.HelloMate.HelloMateBackend.domain.auth.entity.EmailVerification;
import com.HelloMate.HelloMateBackend.domain.auth.entity.EmailVerificationPurpose;
import com.HelloMate.HelloMateBackend.domain.auth.entity.RefreshToken;
import com.HelloMate.HelloMateBackend.domain.auth.repository.RefreshTokenRepository;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
import com.HelloMate.HelloMateBackend.domain.university.entity.University;
import com.HelloMate.HelloMateBackend.domain.university.repository.UniversityRepository;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import com.HelloMate.HelloMateBackend.global.security.Role;
import com.HelloMate.HelloMateBackend.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAuthService {

    private final StudentRepository studentRepository;
    private final UniversityRepository universityRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public StudentSignUpResponse signUp(StudentSignUpRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }
        University university = universityRepository.findById(request.universityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "존재하지 않는 학교입니다."));

        Student student = new Student(
                UuidCreator.create(),
                university,
                request.email(),
                request.name(),
                passwordEncoder.encode(request.password()),
                request.country(),
                request.language(),
                request.studentType(),
                request.major(),
                request.grade()
        );
        studentRepository.save(student);
        return new StudentSignUpResponse(student.getId(), student.getName());
    }

    @Transactional
    public TokenResponse login(StudentLoginRequest request) {
        Student student = studentRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), student.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(student.getId());
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (saved.isExpired() || saved.getRole() != Role.STUDENT) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(saved.getSubjectId(), Role.STUDENT);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(saved.getSubjectId(), Role.STUDENT);
        saved.rotate(newRefreshToken, expiryOf(jwtTokenProvider.getRefreshTokenValidityMs()));
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String studentId) {
        refreshTokenRepository.deleteBySubjectIdAndRole(studentId, Role.STUDENT);
    }

    public void checkEmailAvailable(String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }
    }

    public void sendSignupVerificationCode(String email) {
        emailVerificationService.sendCode(email, EmailVerificationPurpose.SIGNUP);
    }

    public void confirmSignupVerificationCode(String email, String code) {
        emailVerificationService.confirmCode(email, code, EmailVerificationPurpose.SIGNUP);
    }

    public void sendPasswordResetCode(String email) {
        studentRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        emailVerificationService.sendCode(email, EmailVerificationPurpose.PASSWORD_RESET);
    }

    public PasswordResetVerifyResponse verifyPasswordResetCode(String email, String code) {
        EmailVerification verification = emailVerificationService.confirmCode(email, code, EmailVerificationPurpose.PASSWORD_RESET);
        return new PasswordResetVerifyResponse(verification.getResetToken());
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        EmailVerification verification = emailVerificationService.consumeResetToken(resetToken);
        Student student = studentRepository.findByEmail(verification.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        student.updatePassword(passwordEncoder.encode(newPassword));
    }

    private TokenResponse issueTokens(String studentId) {
        String accessToken = jwtTokenProvider.createAccessToken(studentId, Role.STUDENT);
        String refreshToken = jwtTokenProvider.createRefreshToken(studentId, Role.STUDENT);
        refreshTokenRepository.save(new RefreshToken(studentId, Role.STUDENT, refreshToken,
                expiryOf(jwtTokenProvider.getRefreshTokenValidityMs())));
        return new TokenResponse(accessToken, refreshToken);
    }

    private LocalDateTime expiryOf(long validityMs) {
        return LocalDateTime.now().plusNanos(validityMs * 1_000_000);
    }
}
