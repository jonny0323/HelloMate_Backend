package com.HelloMate.HelloMateBackend.domain.auth.controller;

import com.HelloMate.HelloMateBackend.domain.auth.dto.request.RefreshTokenRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StudentLoginRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.request.StudentSignUpRequest;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.StudentSignUpResponse;
import com.HelloMate.HelloMateBackend.domain.auth.dto.response.TokenResponse;
import com.HelloMate.HelloMateBackend.domain.auth.service.StudentAuthService;
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
}
