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
        inviteCode.markUsed();
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
