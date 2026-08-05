package com.HelloMate.HelloMateBackend.domain.chat.entity;

import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private ChatThread thread;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 학생은 모국어로 질문한다. 조회 시 상대방 언어로 번역해주려면 원문 언어를 알아야 한다. */
    @Column(length = 10)
    private String originalLang;

    public ChatMessage(String id, ChatThread thread, SenderType senderType, String content, String originalLang) {
        this.id = id;
        this.thread = thread;
        this.senderType = senderType;
        this.content = content;
        this.originalLang = originalLang;
    }
}
