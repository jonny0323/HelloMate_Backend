package com.HelloMate.HelloMateBackend.domain.translation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TranslateRequest(@NotBlank String text, @NotBlank String targetLang) {
}
