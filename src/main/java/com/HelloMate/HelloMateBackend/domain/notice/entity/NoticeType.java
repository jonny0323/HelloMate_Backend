package com.HelloMate.HelloMateBackend.domain.notice.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NoticeType {
    URGENT, NORMAL;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static NoticeType from(String value) {
        return NoticeType.valueOf(value.toUpperCase());
    }
}
