package com.HelloMate.HelloMateBackend.domain.student.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ERD users.student_type ENUM 그대로 매핑 (설계 문서 1.1절).
 */
public enum StudentType {
    EXCHANGE_STUDENT("교환학생"),
    DEGREE_STUDENT("정규과정생"),
    LANGUAGE_SCHOOL_STUDENT("어학당 수강생"),
    KOREAN_STUDENT("한국인 대학생");

    private final String label;

    StudentType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static StudentType from(String label) {
        for (StudentType type : values()) {
            if (type.label.equals(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("알 수 없는 student_type 입니다: " + label);
    }
}
