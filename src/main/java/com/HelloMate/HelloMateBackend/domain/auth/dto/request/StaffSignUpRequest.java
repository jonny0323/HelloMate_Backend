package com.HelloMate.HelloMateBackend.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StaffSignUpRequest(
        @NotBlank String inviteCode,
        @NotBlank @Email String email,
        @NotBlank String name,
        @NotBlank String position,
        @NotBlank String password
) {
}
