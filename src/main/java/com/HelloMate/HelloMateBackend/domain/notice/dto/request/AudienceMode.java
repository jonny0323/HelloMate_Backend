package com.HelloMate.HelloMateBackend.domain.notice.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
