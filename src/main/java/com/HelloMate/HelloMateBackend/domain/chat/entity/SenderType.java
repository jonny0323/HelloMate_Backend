package com.HelloMate.HelloMateBackend.domain.chat.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SenderType {
    USER, TEACHER;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
