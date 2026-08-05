package com.HelloMate.HelloMateBackend.domain.community.dto.response;

import java.time.LocalDateTime;

public record CreatePostResponse(String id, String authorName, boolean anonymous, LocalDateTime createdAt) {
}
