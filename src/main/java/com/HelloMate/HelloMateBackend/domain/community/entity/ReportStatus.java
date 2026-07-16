package com.HelloMate.HelloMateBackend.domain.community.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReportStatus {
    PENDING, RESOLVED, REJECTED;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static ReportStatus from(String value) {
        return ReportStatus.valueOf(value.toUpperCase());
    }
}
