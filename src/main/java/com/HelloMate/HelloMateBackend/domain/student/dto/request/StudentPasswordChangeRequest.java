package com.HelloMate.HelloMateBackend.domain.student.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StudentPasswordChangeRequest(
        @NotBlank @Pattern(regexp = "\\d{6}") String code,
        @NotBlank String newPassword
) {
}
