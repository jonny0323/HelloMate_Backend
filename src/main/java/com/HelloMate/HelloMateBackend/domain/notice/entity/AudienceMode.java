package com.HelloMate.HelloMateBackend.domain.notice.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 공지 수신 대상 지정 방식. 발송 후에도 발송함에 표시해야 해서 Notice에 스냅샷으로 남는다.
 */
public enum AudienceMode {
    ALL, GROUP, INDIVIDUAL;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static AudienceMode from(String value) {
        return AudienceMode.valueOf(value.toUpperCase());
    }
}
