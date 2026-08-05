package com.HelloMate.HelloMateBackend.domain.admin.service;

import com.HelloMate.HelloMateBackend.domain.admin.dto.response.AdminDashboardResponse;
import com.HelloMate.HelloMateBackend.domain.admin.dto.response.DashboardNoticeItem;
import com.HelloMate.HelloMateBackend.domain.admin.dto.response.DashboardStatsResponse;
import com.HelloMate.HelloMateBackend.domain.admin.dto.response.UnansweredThreadItem;
import com.HelloMate.HelloMateBackend.domain.chat.entity.ChatThread;
import com.HelloMate.HelloMateBackend.domain.chat.repository.ChatThreadRepository;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeStatus;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeReceptionRepository;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeRepository;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    /** 화면의 '최근 발송한 공지'가 4건을 보여준다. 개수 기준을 서버 한 곳에만 둔다. */
    private static final int RECENT_NOTICE_LIMIT = 4;

    private final NoticeRepository noticeRepository;
    private final NoticeReceptionRepository noticeReceptionRepository;
    private final ChatThreadRepository chatThreadRepository;
    private final StudentRepository studentRepository;
    private final StaffService staffService;
    private final SemesterCalculator semesterCalculator;

    public AdminDashboardResponse getDashboard(String staffId) {
        Staff staff = staffService.getStaff(staffId);
        String universityId = staff.getUniversity().getId();

        List<DashboardNoticeItem> recentNotices = noticeRepository
                .findRecentSent(universityId, PageRequest.of(0, RECENT_NOTICE_LIMIT))
                .stream()
                .map(notice -> DashboardNoticeItem.of(notice,
                        noticeReceptionRepository.countByNoticeIdAndReadTrue(notice.getId())))
                .toList();

        List<ChatThread> unanswered = chatThreadRepository
                .findByStaffIdAndStaffUnreadTrueOrderByLastMessageAtDesc(staffId);
        List<UnansweredThreadItem> unansweredThreads = unanswered.stream()
                .map(thread -> new UnansweredThreadItem(thread.getId(), thread.getStudent().getId(),
                        thread.getStudent().getName(), thread.getLastMessage(), thread.getLastMessageAt()))
                .toList();

        return new AdminDashboardResponse(
                buildStats(universityId, staffId, unanswered.size()),
                recentNotices,
                unansweredThreads);
    }

    private DashboardStatsResponse buildStats(String universityId, String staffId, long pendingReplyCount) {
        long sentNoticeCount = noticeRepository.countSentSince(
                universityId, NoticeStatus.SENT, semesterCalculator.currentSemesterStart());

        long totalRecipients = noticeRepository.sumRecipientCount(universityId);
        long totalRead = noticeReceptionRepository.countReadByUniversity(universityId);
        double averageReadRate = totalRecipients == 0 ? 0.0 : (double) totalRead / totalRecipients;

        return new DashboardStatsResponse(
                sentNoticeCount,
                averageReadRate,
                studentRepository.countActiveByUniversityId(universityId),
                pendingReplyCount);
    }
}
