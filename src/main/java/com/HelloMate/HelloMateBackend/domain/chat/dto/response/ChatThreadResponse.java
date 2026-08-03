package com.HelloMate.HelloMateBackend.domain.chat.dto.response;

import java.time.LocalDateTime;

public record ChatThreadResponse(
        String threadId,
        String counterpartId,
        String counterpartName,
        String lastMessage,
        LocalDateTime lastMessageAt,
        boolean unread,
        String noticeId
) {
}
