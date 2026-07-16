package com.HelloMate.HelloMateBackend.domain.honeytip.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateHoneyTipRequest(@NotBlank String category, @NotBlank String title, @NotBlank String content) {
}
