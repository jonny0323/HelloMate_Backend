package com.HelloMate.HelloMateBackend.domain.student.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 이메일 인증 경로와 서류 인증 경로의 결과를 하나로 합친 상태.
 * 마이페이지 '학생 인증됨' 뱃지와 학생 인증 화면의 분기가 이 값 하나만 본다.
 */
public enum StudentVerificationStatus {
    REGISTERED,     // 가입 완료, 아직 학생 인증 전
    DOC_PENDING,    // 서류 제출 후 검토 중
    DOC_REJECTED,   // 서류 반려
    VERIFIED;       // 인증 완료

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static StudentVerificationStatus from(String value) {
        return StudentVerificationStatus.valueOf(value.toUpperCase());
    }
}
