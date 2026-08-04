package com.HelloMate.HelloMateBackend.domain.university.dto.response;

import com.HelloMate.HelloMateBackend.domain.university.entity.Major;

public record MajorResponse(String id, String name) {
    public static MajorResponse from(Major major) {
        return new MajorResponse(major.getId(), major.getName());
    }
}
