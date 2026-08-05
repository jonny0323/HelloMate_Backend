package com.HelloMate.HelloMateBackend.domain.admin.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;

import java.time.LocalDateTime;

public record DashboardNoticeItem(String id, String department, String title, double readRate, LocalDateTime sentAt) {

    public static DashboardNoticeItem of(Notice notice, long readCount) {
        double readRate = notice.getTotalRecipientCount() == 0
                ? 0.0
                : (double) readCount / notice.getTotalRecipientCount();
        return new DashboardNoticeItem(notice.getId(), notice.getDepartment(), notice.getTitle(),
                readRate, notice.getSentAt());
    }
}
