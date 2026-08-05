package com.HelloMate.HelloMateBackend.domain.chat.dto.response;

import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatMessage;
import com.HelloMate.HelloMateBackend.domain.chat.entity.SenderType;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslatedContent;

import java.time.LocalDateTime;

/**
 * translated는 Accept-Language를 보냈고 원문 언어가 다를 때만 채워진다.
 * 학생은 모국어로 질문하고 담당자는 한국어로 읽어야 해서 커뮤니티 글과 동일하게 취급한다.
 */
public record ChatMessageResponse(String id, SenderType senderType, String content, String originalLang,
                                   TranslatedContent translated, LocalDateTime createdAt) {

    public static ChatMessageResponse of(ChatMessage message, TranslatedContent translated) {
        return new ChatMessageResponse(message.getId(), message.getSenderType(), message.getContent(),
                message.getOriginalLang(), translated, message.getCreatedAt());
    }

    public static ChatMessageResponse from(ChatMessage message) {
        return of(message, null);
    }
}
