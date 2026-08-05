package com.HelloMate.HelloMateBackend.domain.chat.dto.response;

import com.HelloMate.HelloMateBackend.domain.chat.entity.ThreadInitiator;

import java.time.LocalDateTime;

public record ChatThreadResponse(
        String threadId,
        String counterpartId,
        String counterpartName,
        String lastMessage,
        LocalDateTime lastMessageAt,
        boolean unread,
        String noticeId,
        ThreadInitiator initiatedBy
) {
}
