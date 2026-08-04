package com.HelloMate.HelloMateBackend.domain.student.dto.request;

public record StudentProfileUpdateRequest(
        String name,
        String country,
        Integer birthYear,
        String language,
        String major,
        String grade
) {
}
