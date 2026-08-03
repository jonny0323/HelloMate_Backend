package com.HelloMate.HelloMateBackend.domain.club.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateClubRequest(
        @NotBlank String title,
        @NotBlank String introduction,
        @Min(1) int maxMembers,
        @NotNull @Future LocalDate deadline
) {
}
