package com.HelloMate.HelloMateBackend.domain.honeytip.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateEditRequest(@NotBlank String content) {
}
