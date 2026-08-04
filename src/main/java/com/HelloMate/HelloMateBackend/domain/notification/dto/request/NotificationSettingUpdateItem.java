package com.HelloMate.HelloMateBackend.domain.notification.dto.request;

import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import jakarta.validation.constraints.NotNull;

public record NotificationSettingUpdateItem(
        @NotNull NotificationCategory category,
        @NotNull Boolean enabled
) {
}
