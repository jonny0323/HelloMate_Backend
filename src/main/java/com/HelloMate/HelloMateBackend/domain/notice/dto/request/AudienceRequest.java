package com.HelloMate.HelloMateBackend.domain.notice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AudienceRequest(@NotNull AudienceMode mode, List<String> groupKeys, List<String> studentIds) {
}
