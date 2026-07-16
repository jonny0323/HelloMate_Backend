package com.HelloMate.HelloMateBackend.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(@NotBlank String content) {
}
