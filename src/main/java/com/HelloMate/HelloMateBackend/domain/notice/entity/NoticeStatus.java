package com.HelloMate.HelloMateBackend.domain.notice.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** 담당자 콘솔의 [임시저장] / [공지 발송하기]에 대응한다. */
public enum NoticeStatus {
    DRAFT, SENT;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static NoticeStatus from(String value) {
        return NoticeStatus.valueOf(value.toUpperCase());
    }
}
