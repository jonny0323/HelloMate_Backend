package com.HelloMate.HelloMateBackend.domain.honeytip.dto.response;

import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTip;

import java.time.LocalDateTime;

public record HoneyTipResponse(
        String id,
        String category,
        String title,
        String content,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static HoneyTipResponse from(HoneyTip honeyTip) {
        return new HoneyTipResponse(honeyTip.getId(), honeyTip.getCategory(), honeyTip.getTitle(),
                honeyTip.getContent(), honeyTip.getViewCount(), honeyTip.getCreatedAt(), honeyTip.getUpdatedAt());
    }
}
