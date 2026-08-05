package com.HelloMate.HelloMateBackend.domain.student.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 탈퇴 계정은 행을 지우지 않고 WITHDRAWN으로만 바꾼다 — post/post_comment가 users를 FK로 물고 있어
 * 물리 삭제하면 커뮤니티 이력이 통째로 깨진다.
 */
public enum StudentStatus {
    ACTIVE, WITHDRAWN;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static StudentStatus from(String value) {
        return StudentStatus.valueOf(value.toUpperCase());
    }
}
