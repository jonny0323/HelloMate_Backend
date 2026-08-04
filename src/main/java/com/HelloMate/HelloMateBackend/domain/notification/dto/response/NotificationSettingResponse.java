package com.HelloMate.HelloMateBackend.domain.notification.dto.response;

import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;

public record NotificationSettingResponse(NotificationCategory category, boolean enabled, boolean required) {
}
