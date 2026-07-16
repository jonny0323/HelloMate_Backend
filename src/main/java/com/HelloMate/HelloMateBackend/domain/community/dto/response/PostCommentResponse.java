package com.HelloMate.HelloMateBackend.domain.community.dto.response;

import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslatedContent;

import java.time.LocalDateTime;

public record PostCommentResponse(
        String id,
        String parentCommentId,
        String anonName,
        String content,
        String originalLang,
        TranslatedContent translated,
        int likeCount,
        boolean likedByMe,
        LocalDateTime createdAt
) {
}
