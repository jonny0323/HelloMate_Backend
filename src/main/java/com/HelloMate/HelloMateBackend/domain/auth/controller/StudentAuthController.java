package com.HelloMate.HelloMateBackend.domain.auth.controller;

import com.HelloMate.HelloMateBackend.domain.auth.dto.request.CheckEmailRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.EmailVerificationRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.PasswordResetEmailRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.PasswordResetRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.PasswordResetVerifyRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.RefreshTokenRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StudentLoginRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StudentSignUpRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.PasswordResetVerifyResponse;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.StudentSignUpResponse;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.TokenResponse;
import com.HelloMate.HelloMateBackend.domain.auth.service.StudentAuthService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/students")
@RequiredArgsConstructor
public class StudentAuthController {

    private final StudentAuthService studentAuthService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StudentSignUpResponse> signUp(@Valid @RequestBody StudentSignUpRequest request) {
        return ApiResponse.ok(studentAuthService.signUp(request));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody StudentLoginRequest request) {
        return ApiResponse.ok(studentAuthService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(studentAuthService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@CurrentUser AuthPrincipal principal) {
        studentAuthService.logout(principal.id());
        return ApiResponse.ok(null);
    }

    @PostMapping("/check-email")
    public ApiResponse<Void> checkEmail(@Valid @RequestBody CheckEmailRequest request) {
        studentAuthService.checkEmailAvailable(request.email());
        return ApiResponse.ok(null);
    }

    @PostMapping("/email-verifications")
    public ApiResponse<Void> sendSignupVerification(@Valid @RequestBody EmailVerificationRequest request) {
        studentAuthService.sendSignupVerificationCode(request.email());
        return ApiResponse.ok(null);
    }

    @PostMapping("/email-verifications/confirm")
    public ApiResponse<Void> confirmSignupVerification(@Valid @RequestBody EmailVerificationConfirmRequest request) {
        studentAuthService.confirmSignupVerificationCode(request.email(), request.code());
        return ApiResponse.ok(null);
    }

    @PostMapping("/password-reset/email")
    public ApiResponse<Void> sendPasswordResetCode(@Valid @RequestBody PasswordResetEmailRequest request) {
        studentAuthService.sendPasswordResetCode(request.email());
        return ApiResponse.ok(null);
    }

    @PostMapping("/password-reset/verify")
    public ApiResponse<PasswordResetVerifyResponse> verifyPasswordResetCode(@Valid @RequestBody PasswordResetVerifyRequest request) {
        return ApiResponse.ok(studentAuthService.verifyPasswordResetCode(request.email(), request.code()));
    }

    @PatchMapping("/password-reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        studentAuthService.resetPassword(request.resetToken(), request.newPassword());
        return ApiResponse.ok(null);
    }
}
