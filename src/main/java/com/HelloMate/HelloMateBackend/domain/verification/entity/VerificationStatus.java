package com.HelloMate.HelloMateBackend.domain.verification.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VerificationStatus {
    PENDING, APPROVED, REJECTED;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static VerificationStatus from(String value) {
        return VerificationStatus.valueOf(value.toUpperCase());
    }
}
