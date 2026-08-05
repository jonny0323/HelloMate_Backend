package com.HelloMate.HelloMateBackend.domain.notice.service;

import com.HelloMate.HelloMateBackend.domain.file.entity.UploadedFile;
import com.HelloMate.HelloMateBackend.domain.file.repository.UploadedFileRepository;
import com.HelloMate.HelloMateBackend.domain.notice.dto.request.AudienceRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.request.CreateNoticeRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.request.SaveDraftRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.request.UpdateNoticeRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AdminNoticeDetailResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AdminNoticeSummaryResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AudienceCountResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.CreateNoticeResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.DraftResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeFileResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeReceptionResponse;
import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeFile;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeFileRepository;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeReceptionRepository;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeRepository;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import com.HelloMate.HelloMateBackend.domain.notification.service.NotificationService;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    /** 담당자 콘솔 첨부 영역이 안내하는 형식. 서버도 같은 기준으로 막는다. */
    private static final int MAX_ATTACHMENTS = 5;
    private static final List<String> ALLOWED_ATTACHMENT_TYPES = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "image/jpeg", "image/png", "image/heic");

    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final NoticeReceptionRepository noticeReceptionRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final StaffService staffService;
    private final NotificationService notificationService;
    private final NoticeAudienceResolver audienceResolver;

    /* ===================== 발송 ===================== */

    /**
     * 부서는 요청이 아니라 작성자에게서 가져온다(부서 사칭 방지).
     * 수신자가 0명이면 {@link NoticeAudienceResolver}가 막는다.
     */
    @Transactional
    public CreateNoticeResponse createAndSend(String staffId, CreateNoticeRequest request) {
        Staff author = staffService.getStaff(staffId);

        // 즉시 발송도 초안으로 만들어 dispatch()의 markSent()로 확정한다 — 발송 확정 지점을 하나로 유지.
        // PK를 앱에서 채워 넣으므로 save()는 persist가 아니라 merge를 탄다. 인자로 넘긴 인스턴스는
        // detached로 남아 이후 변경(markSent 등)이 flush되지 않으니, 반환된 관리 인스턴스를 써야 한다.
        Notice notice = noticeRepository.save(
                Notice.draft(UuidCreator.create(), author, request.title(), request.content(), request.type()));
        notice.assignBannerPeriod(request.bannerStartDate(), request.bannerEndDate());

        attachFiles(notice, request.files());
        int recipientCount = dispatch(notice, author, request.audience());

        return new CreateNoticeResponse(notice.getId(), recipientCount, notice.getSentAt());
    }

    /** 임시저장했던 초안을 발송한다. */
    @Transactional
    public CreateNoticeResponse sendDraft(String staffId, String noticeId, CreateNoticeRequest request) {
        Staff author = staffService.getStaff(staffId);
        Notice notice = getEditableNotice(author, noticeId);
        if (!notice.isDraft()) {
            throw new BusinessException(ErrorCode.NOTICE_ALREADY_SENT);
        }

        notice.updateContent(request.title(), request.content(), request.type());
        notice.assignBannerPeriod(request.bannerStartDate(), request.bannerEndDate());
        attachFiles(notice, request.files());
        int recipientCount = dispatch(notice, author, request.audience());

        return new CreateNoticeResponse(notice.getId(), recipientCount, notice.getSentAt());
    }

    /** 발송 전 실제 수신자 수를 알려준다. 화면이 그룹 인원을 합산하면 중복 인원이 이중 계산된다. */
    public AudienceCountResponse countAudience(String staffId, AudienceRequest audience) {
        Staff staff = staffService.getStaff(staffId);
        List<Student> recipients = audienceResolver.resolve(staff.getUniversity().getId(), audience);
        return new AudienceCountResponse(recipients.size(), audienceResolver.describe(audience, recipients.size()));
    }

    /* ===================== 임시저장 ===================== */

    @Transactional
    public DraftResponse saveDraft(String staffId, SaveDraftRequest request) {
        Staff author = staffService.getStaff(staffId);
        Notice draft = Notice.draft(UuidCreator.create(), author, request.title(), request.content(), request.type());
        return DraftResponse.from(noticeRepository.save(draft));
    }

    @Transactional
    public DraftResponse updateDraft(String staffId, String noticeId, SaveDraftRequest request) {
        Staff author = staffService.getStaff(staffId);
        Notice draft = getEditableNotice(author, noticeId);
        if (!draft.isDraft()) {
            throw new BusinessException(ErrorCode.NOTICE_ALREADY_SENT);
        }
        draft.updateContent(request.title(), request.content(), request.type());
        return DraftResponse.from(draft);
    }

    public List<DraftResponse> getMyDrafts(String staffId) {
        return noticeRepository.findMyDrafts(staffId).stream()
                .map(DraftResponse::from)
                .toList();
    }

    /* ===================== 발송함 ===================== */

    public Page<AdminNoticeSummaryResponse> getSentNotices(String staffId, String department, String keyword,
                                                            int page, int size) {
        Staff staff = staffService.getStaff(staffId);
        Page<Notice> notices = noticeRepository.searchForAdmin(staff.getUniversity().getId(), department, keyword,
                PageRequest.of(Math.max(page - 1, 0), size));
        return notices.map(notice -> AdminNoticeSummaryResponse.of(notice,
                noticeReceptionRepository.countByNoticeIdAndReadTrue(notice.getId()),
                notice.isManageableBy(staff)));
    }

    public AdminNoticeDetailResponse getNoticeDetail(String staffId, String noticeId) {
        Staff staff = staffService.getStaff(staffId);
        Notice notice = getReadableNotice(staff, noticeId);
        long readCount = noticeReceptionRepository.countByNoticeIdAndReadTrue(noticeId);
        List<NoticeFileResponse> files = noticeFileRepository.findByNoticeId(noticeId).stream()
                .map(NoticeFileResponse::from)
                .toList();
        return AdminNoticeDetailResponse.of(notice, readCount, notice.isManageableBy(staff), files);
    }

    public Page<NoticeReceptionResponse> getReceptions(String staffId, String noticeId, int page, int size) {
        Staff staff = staffService.getStaff(staffId);
        getReadableNotice(staff, noticeId);
        return noticeReceptionRepository
                .findByNoticeId(noticeId, PageRequest.of(Math.max(page - 1, 0), size))
                .map(NoticeReceptionResponse::from);
    }

    @Transactional
    public AdminNoticeDetailResponse update(String staffId, String noticeId, UpdateNoticeRequest request) {
        Staff staff = staffService.getStaff(staffId);
        Notice notice = getEditableNotice(staff, noticeId);
        notice.updateContent(request.title(), request.content(), request.type());
        notice.assignBannerPeriod(request.bannerStartDate(), request.bannerEndDate());

        long readCount = noticeReceptionRepository.countByNoticeIdAndReadTrue(noticeId);
        List<NoticeFileResponse> files = noticeFileRepository.findByNoticeId(noticeId).stream()
                .map(NoticeFileResponse::from)
                .toList();
        return AdminNoticeDetailResponse.of(notice, readCount, true, files);
    }

    /**
     * 재발송 = 아직 안 읽은 학생에게 알림을 다시 보내는 것.
     * 예전 구현은 모든 수신자의 read를 false로 되돌렸는데, 그러면 이미 읽은 학생의 readAt이 사라지고
     * 열람률이 0%로 리셋되면서 정작 학생에게는 아무것도 가지 않았다.
     */
    @Transactional
    public int resend(String staffId, String noticeId) {
        Staff staff = staffService.getStaff(staffId);
        Notice notice = getEditableNotice(staff, noticeId);
        notice.recordResend();

        List<NoticeReception> unread = noticeReceptionRepository.findByNoticeIdAndReadFalse(noticeId);
        notificationService.notifyAll(
                unread.stream().map(NoticeReception::getStudent).toList(),
                NotificationCategory.NOTICE, notice.getTitle(), "notice", notice.getId());
        return unread.size();
    }

    /**
     * 소프트 삭제. chat_thread가 notice를 FK로 참조하고 있어 물리 삭제는 FK 위반이고,
     * 학생이 이미 읽은 공지의 열람 이력도 남겨야 한다.
     */
    @Transactional
    public void delete(String staffId, String noticeId) {
        Staff staff = staffService.getStaff(staffId);
        Notice notice = getEditableNotice(staff, noticeId);
        notice.softDelete();
    }

    /* ===================== 내부 ===================== */

    /** 수신자 생성 + 알림을 배치로 처리한다. 학생 수만큼 단건 저장하면 312명에 쿼리 900회가 넘는다. */
    private int dispatch(Notice notice, Staff author, AudienceRequest audience) {
        List<Student> recipients = audienceResolver.resolve(author.getUniversity().getId(), audience);

        List<NoticeReception> receptions = recipients.stream()
                .map(student -> new NoticeReception(UuidCreator.create(), notice, student))
                .toList();
        noticeReceptionRepository.saveAll(receptions);

        notice.markSent(recipients.size(), audience.mode(),
                audienceResolver.describe(audience, recipients.size()));

        notificationService.notifyAll(recipients, NotificationCategory.NOTICE, notice.getTitle(),
                "notice", notice.getId());
        return recipients.size();
    }

    private void attachFiles(Notice notice, List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        if (fileIds.size() > MAX_ATTACHMENTS) {
            throw new BusinessException(ErrorCode.NOTICE_ATTACHMENT_LIMIT);
        }
        for (String fileId : fileIds) {
            UploadedFile file = uploadedFileRepository.findById(fileId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
            if (!ALLOWED_ATTACHMENT_TYPES.contains(file.getContentType().toLowerCase(Locale.ROOT))) {
                throw new BusinessException(ErrorCode.NOTICE_ATTACHMENT_TYPE);
            }
            noticeFileRepository.save(new NoticeFile(UuidCreator.create(), notice, file));
        }
    }

    /** 조회 권한: 같은 대학이면 열람 가능(다른 부서 공지도 참고할 수 있어야 한다). */
    private Notice getReadableNotice(Staff staff, String noticeId) {
        Notice notice = getNotice(noticeId);
        if (!notice.isSameUniversity(staff.getUniversity().getId())) {
            throw new BusinessException(ErrorCode.NOT_MY_UNIVERSITY);
        }
        return notice;
    }

    /** 변경 권한: 같은 부서만. 남의 부서 공지를 지우거나 다시 밀어 넣을 수 없다. */
    private Notice getEditableNotice(Staff staff, String noticeId) {
        Notice notice = getReadableNotice(staff, noticeId);
        if (!notice.isSameDepartment(staff.getDepartment())) {
            throw new BusinessException(ErrorCode.NOT_MY_DEPARTMENT);
        }
        return notice;
    }

    public Notice getNotice(String noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        if (notice.isDeleted()) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_FOUND);
        }
        return notice;
    }
}
