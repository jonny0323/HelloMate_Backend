package com.HelloMate.HelloMateBackend.domain.community.dto.response;

import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslatedContent;

import java.util.List;

public record PostDetailResponse(
        String id,
        String anonName,
        String title,
        String originalLang,
        String content,
        TranslatedContent translated,
        int likeCount,
        boolean likedByMe,
        int commentCount,
        List<PostCommentResponse> comments
) {
}
