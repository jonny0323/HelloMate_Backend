package com.HelloMate.HelloMateBackend.domain.auth.controller;

import com.HelloMate.HelloMateBackend.domain.auth.dto.request.RefreshTokenRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StaffLoginRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StaffSignUpRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.StaffSignUpResponse;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.TokenResponse;
import com.HelloMate.HelloMateBackend.domain.auth.service.StaffAuthService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/staff")
@RequiredArgsConstructor
public class StaffAuthController {

    private final StaffAuthService staffAuthService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StaffSignUpResponse> signUp(@Valid @RequestBody StaffSignUpRequest request) {
        return ApiResponse.ok(staffAuthService.signUp(request));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody StaffLoginRequest request) {
        return ApiResponse.ok(staffAuthService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(staffAuthService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@CurrentUser AuthPrincipal principal) {
        staffAuthService.logout(principal.id());
        return ApiResponse.ok(null);
    }
}
