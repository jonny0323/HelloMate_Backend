package com.HelloMate.HelloMateBackend.domain.honeytip.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EditRequestStatus {
    PENDING, APPROVED, REJECTED;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static EditRequestStatus from(String value) {
        return EditRequestStatus.valueOf(value.toUpperCase());
    }
}
