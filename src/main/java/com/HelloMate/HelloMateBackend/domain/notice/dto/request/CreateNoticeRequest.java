package com.HelloMate.HelloMateBackend.domain.notice.dto.request;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateNoticeRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotBlank String department,
        @NotNull NoticeType type,
        @NotNull @Valid AudienceRequest audience,
        List<String> files
) {
}
