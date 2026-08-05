package com.HelloMate.HelloMateBackend.domain.notice.dto.request;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;
import jakarta.validation.constraints.Size;

/**
 * 임시저장. 작성 중이라 제목/내용이 비어 있을 수 있어 필수 검증을 걸지 않는다.
 * 수신 대상은 발송 시점에 확정하므로 초안에는 담지 않는다.
 */
public record SaveDraftRequest(
        @Size(max = 255) String title,
        String content,
        NoticeType type
) {
}
