package com.HelloMate.HelloMateBackend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CheckLoginIdRequest(@NotBlank String loginId) {
}
