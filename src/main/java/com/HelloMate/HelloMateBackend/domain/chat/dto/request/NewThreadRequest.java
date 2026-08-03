package com.HelloMate.HelloMateBackend.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NewThreadRequest(@NotBlank String teacherId, String noticeId, @NotBlank String message) {
}
