package com.HelloMate.HelloMateBackend.domain.honeytip.dto.request;

import com.HelloMate.HelloMateBackend.domain.honeytip.entity.EditRequestStatus;
import jakarta.validation.constraints.NotNull;

public record ReviewEditRequest(@NotNull EditRequestStatus status) {
}
