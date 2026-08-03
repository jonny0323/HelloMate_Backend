package com.HelloMate.HelloMateBackend.domain.auth.dto.request;

import com.HelloMate.HelloMateBackend.domain.student.entity.StudentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentSignUpRequest(
        @NotBlank @Email String email,
        @NotBlank String name,
        @NotBlank String password,
        @NotBlank String country,
        @NotBlank String language,
        @NotNull StudentType studentType,
        String major,
        String grade,
        @NotBlank String universityId
) {
}
