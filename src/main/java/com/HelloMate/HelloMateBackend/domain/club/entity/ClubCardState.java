package com.HelloMate.HelloMateBackend.domain.club.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 클럽 목록 카드의 3가지 표시 상태(참여 가능 / 참여 중 / 마감).
 * 클라이언트마다 판정 로직이 갈리지 않도록 서버가 계산해서 내려준다.
 */
public enum ClubCardState {
    JOINABLE, JOINED, CLOSED;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static ClubCardState from(String value) {
        return ClubCardState.valueOf(value.toUpperCase());
    }
}
