package com.HelloMate.HelloMateBackend.domain.chat.dto.response;

import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatMessage;
import com.HelloMate.HelloMateBackend.domain.chat.entity.SenderType;

import java.time.LocalDateTime;

public record ChatMessageResponse(String id, SenderType senderType, String content, LocalDateTime createdAt) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(message.getId(), message.getSenderType(), message.getContent(), message.getCreatedAt());
    }
}
