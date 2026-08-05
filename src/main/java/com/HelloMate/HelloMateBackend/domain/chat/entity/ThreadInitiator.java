package com.HelloMate.HelloMateBackend.domain.chat.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 스레드를 누가 열었는지. 담당자가 먼저 건 대화는 학생 입장에서 '요청한 적 없는 DM'이라
 * 나중에 알림/차단 정책을 다르게 걸 수 있어야 한다.
 */
public enum ThreadInitiator {
    STUDENT, STAFF;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static ThreadInitiator from(String value) {
        return ThreadInitiator.valueOf(value.toUpperCase());
    }
}
