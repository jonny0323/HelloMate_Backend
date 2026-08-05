package com.HelloMate.HelloMateBackend.domain.verification.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** 서류 인증 화면의 '인증 가능한 서류' 4종 (최근 3개월 이내 발급분). */
public enum DocumentType {
    STUDENT_ID,              // 학생증(모바일 포함)
    ENROLLMENT_CERTIFICATE,  // 재학증명서
    ADMISSION_LETTER,        // 입학허가서
    TRANSCRIPT;              // 성적증명서

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static DocumentType from(String value) {
        return DocumentType.valueOf(value.toUpperCase());
    }
}
