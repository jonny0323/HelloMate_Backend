package com.HelloMate.HelloMateBackend.domain.admin.dto.response;

import java.time.LocalDateTime;

/** 화면의 '답변이 필요한 학생' 카드. 클릭하면 해당 스레드로 바로 들어갈 수 있게 threadId를 준다. */
public record UnansweredThreadItem(String threadId, String studentId, String studentName,
                                    String lastMessage, LocalDateTime lastMessageAt) {
}
