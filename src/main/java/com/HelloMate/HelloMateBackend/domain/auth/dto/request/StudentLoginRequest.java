package com.HelloMate.HelloMateBackend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/** autoLogin은 로그인 화면의 '자동 로그인' 체크박스 — 리프레시 토큰 수명을 30일/1일로 가른다. */
public record StudentLoginRequest(@NotBlank String loginId, @NotBlank String password, boolean autoLogin) {
}
