package com.HelloMate.HelloMateBackend.domain.notification.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * CHAT_DIRECT/CHAT_CLUB은 설정 토글로만 존재한다 — 채팅은 이미 자체 read/unread(ChatThread,
 * unread-count)가 있어서 이 카테고리로 Notification 행을 만들지 않는다 (설계: docs/roadmaps/notification.md).
 */
public enum NotificationCategory {
    CHAT_DIRECT(false),
    CHAT_CLUB(false),
    NOTICE(false),
    COMMUNITY(false),
    CLUB(false),
    HONEY_TIP(false),
    SYSTEM(true);

    private final boolean required;

    NotificationCategory(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static NotificationCategory from(String value) {
        return NotificationCategory.valueOf(value.toUpperCase());
    }
}
