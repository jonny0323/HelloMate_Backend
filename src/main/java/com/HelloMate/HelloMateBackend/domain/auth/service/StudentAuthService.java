package com.HelloMate.HelloMateBackend.domain.auth.service;

import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StudentLoginRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StudentSignUpRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.AvailabilityResponse;
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
        if (studentRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (studentRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }
        University university = universityRepository.findById(request.universityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "존재하지 않는 학교입니다."));

        Student student = new Student(
                UuidCreator.create(),
                university,
                request.loginId(),
                request.email(),
                request.name(),
                passwordEncoder.encode(request.password()),
                request.country(),
                request.language(),
                request.studentType(),
                request.major(),
                request.grade(),
                request.birthYear()
        );
        student.agreeTerms();
        studentRepository.save(student);
        return new StudentSignUpResponse(student.getId(), student.getName());
    }

    /**
     * 아이디/비밀번호 중 무엇이 틀렸는지 구분하지 않는다 — 구분해서 알려주면 가입된 아이디 목록을
     * 긁어낼 수 있다(계정 열거). 디자인의 오류 문구도 하나로 통일되어 있다.
     */
    @Transactional
    public TokenResponse login(StudentLoginRequest request) {
        Student student = studentRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (student.isWithdrawn()) {
            throw new BusinessException(ErrorCode.ACCOUNT_WITHDRAWN);
        }
        if (student.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (!passwordEncoder.matches(request.password(), student.getPassword())) {
            student.increaseLoginFailCount();
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        student.unlock();
        return issueTokens(student.getId(), request.autoLogin());
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

    public AvailabilityResponse checkLoginIdAvailable(String loginId) {
        if (studentRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        return AvailabilityResponse.available("사용할 수 있는 아이디예요");
    }

    public AvailabilityResponse checkEmailAvailable(String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }
        return AvailabilityResponse.available("사용할 수 있는 이메일이에요");
    }

    public void sendSignupVerificationCode(String email) {
        emailVerificationService.sendCode(email, EmailVerificationPurpose.SIGNUP);
    }

    /**
     * 이메일 인증은 학생 인증의 두 경로 중 하나다. 성공하면 해당 이메일의 계정을 곧바로 VERIFIED로
     * 올린다. 가입 도중(계정 생성 전)이라 계정이 아직 없으면 코드 확인만 하고 넘어간다.
     */
    @Transactional
    public void confirmSignupVerificationCode(String email, String code) {
        emailVerificationService.confirmCode(email, code, EmailVerificationPurpose.SIGNUP);
        studentRepository.findByEmail(email).ifPresent(Student::verifyByEmail);
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

    /** 비밀번호를 바꾸면 기존 세션을 전부 끊는다 — 계정을 탈취당한 상태에서 되찾는 경로이기 때문. */
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        EmailVerification verification = emailVerificationService.consumeResetToken(resetToken);
        Student student = studentRepository.findByEmail(verification.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        student.updatePassword(passwordEncoder.encode(newPassword));
        refreshTokenRepository.deleteBySubjectIdAndRole(student.getId(), Role.STUDENT);
    }

    private TokenResponse issueTokens(String studentId, boolean autoLogin) {
        String accessToken = jwtTokenProvider.createAccessToken(studentId, Role.STUDENT);
        String refreshToken = jwtTokenProvider.createRefreshToken(studentId, Role.STUDENT, autoLogin);
        refreshTokenRepository.save(new RefreshToken(studentId, Role.STUDENT, refreshToken,
                expiryOf(jwtTokenProvider.getRefreshTokenValidityMs(autoLogin))));
        return new TokenResponse(accessToken, refreshToken);
    }

    private LocalDateTime expiryOf(long validityMs) {
        return LocalDateTime.now().plusNanos(validityMs * 1_000_000);
    }
}
