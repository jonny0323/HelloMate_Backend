package com.HelloMate.HelloMateBackend.domain.club.dto.response;

import com.HelloMate.HelloMateBackend.domain.club.entity.ClubMessage;

import java.time.LocalDateTime;

public record ClubMessageResponse(String id, String senderId, String senderName, String content, LocalDateTime createdAt) {
    public static ClubMessageResponse from(ClubMessage message) {
        return new ClubMessageResponse(message.getId(), message.getSender().getId(), message.getSender().getName(),
                message.getContent(), message.getCreatedAt());
    }
}
