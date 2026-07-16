package com.HelloMate.HelloMateBackend.domain.admin.service;

import com.HelloMate.HelloMateBackend.domain.admin.dto.response.AdminDashboardResponse;
import com.HelloMate.HelloMateBackend.domain.admin.dto.response.DashboardNoticeItem;
import com.HelloMate.HelloMateBackend.domain.admin.dto.response.UnansweredThreadItem;
import com.HelloMate.HelloMateBackend.domain.chat.repository.ChatThreadRepository;
import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeReceptionRepository;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeRepository;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final NoticeRepository noticeRepository;
    private final NoticeReceptionRepository noticeReceptionRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final StaffService staffService;

    public AdminDashboardResponse getDashboard(String staffId) {
        Staff staff = staffService.getStaff(staffId);

        var recentNotices = noticeRepository.findTop4ByUniversityIdOrderByCreatedAtDesc(staff.getUniversity().getId())
                .stream()
                .map(this::toDashboardNoticeItem)
                .toList();

        var unansweredThreads = chatThreadRepository.findByStaffIdAndStaffUnreadTrueOrderByLastMessageAtDesc(staffId)
                .stream()
                .map(thread -> new UnansweredThreadItem(thread.getStudent().getId(), thread.getStudent().getName(),
                        thread.getLastMessage()))
                .toList();

        return new AdminDashboardResponse(recentNotices, unansweredThreads);
    }

    private DashboardNoticeItem toDashboardNoticeItem(Notice notice) {
        long readCount = noticeReceptionRepository.countByNoticeIdAndReadTrue(notice.getId());
        double readRate = notice.getTotalRecipientCount() == 0 ? 0.0 : (double) readCount / notice.getTotalRecipientCount();
        return new DashboardNoticeItem(notice.getId(), notice.getTitle(), readRate);
    }
}
