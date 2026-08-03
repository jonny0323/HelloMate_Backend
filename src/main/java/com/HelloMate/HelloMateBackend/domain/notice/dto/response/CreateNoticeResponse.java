package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import java.time.LocalDateTime;

public record CreateNoticeResponse(String id, int totalRecipientCount, LocalDateTime createdAt) {
}
