package com.HelloMate.HelloMateBackend.domain.auth.service;

import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StaffLoginRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StaffSignUpRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.StaffSignUpResponse;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.TokenResponse;
import com.HelloMate.HelloMateBackend.domain.auth.entity.RefreshToken;
import com.HelloMate.HelloMateBackend.domain.auth.repository.RefreshTokenRepository;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.entity.StaffInviteCode;
import com.HelloMate.HelloMateBackend.domain.staff.repository.StaffInviteCodeRepository;
import com.HelloMate.HelloMateBackend.domain.staff.repository.StaffRepository;
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
public class StaffAuthService {

    private final StaffRepository staffRepository;
    private final StaffInviteCodeRepository staffInviteCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public StaffSignUpResponse signUp(StaffSignUpRequest request) {
        if (staffRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }
        StaffInviteCode inviteCode = staffInviteCodeRepository.findByCode(request.inviteCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));
        if (inviteCode.isUsed() || inviteCode.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);
        }

        Staff staff = new Staff(
                UuidCreator.create(),
                inviteCode.getUniversity(),
                request.email(),
                request.name(),
                request.position(),
                inviteCode.getDepartment(),
                passwordEncoder.encode(request.password()),
                inviteCode
        );
        // 초대 코드 자체가 승인 절차다 — 부서 관리자가 발급한 1회용 코드를 가진 사람만 가입할 수 있으므로
        // 코드를 소진하는 시점에 승인 상태로 올린다. (별도 승인 화면을 두면 코드 발급이 무의미해진다)
        inviteCode.markUsed();
        staff.approve();
        staffRepository.save(staff);
        return new StaffSignUpResponse(staff.getId(), staff.getName(), staff.isVerified());
    }

    @Transactional
    public TokenResponse login(StaffLoginRequest request) {
        Staff staff = staffRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), staff.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        // 승인되지 않은 계정은 /admin/** 전체에 접근하게 되므로 토큰 자체를 발급하지 않는다.
        if (!staff.isVerified()) {
            throw new BusinessException(ErrorCode.STAFF_NOT_VERIFIED);
        }
        return issueTokens(staff.getId());
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (saved.isExpired() || saved.getRole() != Role.STAFF) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(saved.getSubjectId(), Role.STAFF);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(saved.getSubjectId(), Role.STAFF);
        saved.rotate(newRefreshToken, expiryOf(jwtTokenProvider.getRefreshTokenValidityMs()));
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String staffId) {
        refreshTokenRepository.deleteBySubjectIdAndRole(staffId, Role.STAFF);
    }

    private TokenResponse issueTokens(String staffId) {
        String accessToken = jwtTokenProvider.createAccessToken(staffId, Role.STAFF);
        String refreshToken = jwtTokenProvider.createRefreshToken(staffId, Role.STAFF);
        refreshTokenRepository.save(new RefreshToken(staffId, Role.STAFF, refreshToken,
                expiryOf(jwtTokenProvider.getRefreshTokenValidityMs())));
        return new TokenResponse(accessToken, refreshToken);
    }

    private LocalDateTime expiryOf(long validityMs) {
        return LocalDateTime.now().plusNanos(validityMs * 1_000_000);
    }
}
