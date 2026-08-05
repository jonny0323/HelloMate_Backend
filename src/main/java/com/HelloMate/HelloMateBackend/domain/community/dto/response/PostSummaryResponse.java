package com.HelloMate.HelloMateBackend.domain.community.dto.response;

import com.HelloMate.HelloMateBackend.domain.community.entity.Post;

import java.time.LocalDateTime;

/** authorName은 익명 글이면 "익명 N", 실명 글이면 작성자 이름이다. */
public record PostSummaryResponse(
        String id,
        String authorName,
        boolean anonymous,
        String title,
        String content,
        String originalLang,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse of(Post post, String authorName) {
        return new PostSummaryResponse(post.getId(), authorName, post.isAnonymous(), post.getTitle(), post.getContent(),
                post.getOriginalLang(), post.getLikeCount(), post.getCommentCount(), post.getCreatedAt());
    }
}
