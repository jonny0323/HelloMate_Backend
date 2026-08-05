package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;

import java.time.LocalDateTime;

/**
 * canManage는 발송함 행의 [재발송]/[삭제] 버튼 노출 여부다. 다른 부서가 보낸 공지는 읽기만 된다 —
 * 클라이언트가 부서명을 비교해 판단하지 않도록 서버가 계산해서 내려준다.
 */
public record AdminNoticeSummaryResponse(
        String id,
        String title,
        String department,
        NoticeType type,
        String audienceLabel,
        int totalRecipientCount,
        long readCount,
        double readRate,
        int resendCount,
        boolean canManage,
        LocalDateTime sentAt
) {
    public static AdminNoticeSummaryResponse of(Notice notice, long readCount, boolean canManage) {
        double readRate = notice.getTotalRecipientCount() == 0
                ? 0.0
                : (double) readCount / notice.getTotalRecipientCount();
        return new AdminNoticeSummaryResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getDepartment(),
                notice.getType(),
                notice.getAudienceLabel(),
                notice.getTotalRecipientCount(),
                readCount,
                readRate,
                notice.getResendCount(),
                canManage,
                notice.getSentAt()
        );
    }
}
