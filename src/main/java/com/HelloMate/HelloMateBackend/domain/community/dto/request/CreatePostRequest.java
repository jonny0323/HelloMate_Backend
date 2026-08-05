package com.HelloMate.HelloMateBackend.domain.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * anonymous는 작성 화면의 익명/실명 토글. 생략하면 익명으로 본다 —
 * 실수로 실명이 노출되는 것보다 익명으로 올라가는 쪽이 안전하다.
 */
public record CreatePostRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 5000) String content,
        Boolean anonymous
) {
    public boolean isAnonymous() {
        return anonymous == null || anonymous;
    }
}
