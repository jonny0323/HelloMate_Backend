package com.HelloMate.HelloMateBackend.domain.notification.dto.response;

import com.HelloMate.HelloMateBackend.domain.notification.entity.Notification;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        NotificationCategory category,
        String title,
        String linkType,
        String linkId,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getCategory(), notification.getTitle(),
                notification.getLinkType(), notification.getLinkId(), notification.isRead(), notification.getCreatedAt());
    }
}
