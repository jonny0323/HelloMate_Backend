package com.HelloMate.HelloMateBackend.domain.community.dto.response;

import com.HelloMate.HelloMateBackend.domain.community.entity.Post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        String id,
        String anonName,
        String title,
        String content,
        String originalLang,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse of(Post post, String anonName) {
        return new PostSummaryResponse(post.getId(), anonName, post.getTitle(), post.getContent(),
                post.getOriginalLang(), post.getLikeCount(), post.getCommentCount(), post.getCreatedAt());
    }
}
