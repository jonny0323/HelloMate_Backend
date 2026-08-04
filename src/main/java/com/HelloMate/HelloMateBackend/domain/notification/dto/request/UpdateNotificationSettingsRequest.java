package com.HelloMate.HelloMateBackend.domain.notification.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateNotificationSettingsRequest(
        @NotEmpty @Valid List<NotificationSettingUpdateItem> settings
) {
}
