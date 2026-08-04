package com.HelloMate.HelloMateBackend.domain.club.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SendClubMessageRequest(@NotBlank String content) {
}
