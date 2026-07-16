package com.HelloMate.HelloMateBackend.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StaffLoginRequest(@NotBlank @Email String email, @NotBlank String password) {
}
