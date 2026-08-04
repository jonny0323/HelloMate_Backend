package com.HelloMate.HelloMateBackend.domain.community.dto.response;

import java.time.LocalDateTime;

public record MyCommentResponse(
        String id,
        String postId,
        String parentCommentId,
        String anonName,
        String content,
        String originalLang,
        int likeCount,
        LocalDateTime createdAt
) {
}
