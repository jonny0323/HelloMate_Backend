package com.HelloMate.HelloMateBackend.domain.notice.service;

import com.HelloMate.HelloMateBackend.domain.file.entity.UploadedFile;
import com.HelloMate.HelloMateBackend.domain.file.repository.UploadedFileRepository;
import com.HelloMate.HelloMateBackend.domain.notice.dto.request.CreateNoticeRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AdminNoticeDetailResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AdminNoticeSummaryResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.CreateNoticeResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeFileResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeReceptionResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.request.AudienceRequest;
import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeFile;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeFileRepository;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeReceptionRepository;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeRepository;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
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

    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final NoticeReceptionRepository noticeReceptionRepository;
    private final StudentRepository studentRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final StaffService staffService;

    @Transactional
    public CreateNoticeResponse createAndSend(String staffId, CreateNoticeRequest request) {
        Staff author = staffService.getStaff(staffId);

        Notice notice = new Notice(UuidCreator.create(), author.getUniversity(), author,
                request.title(), request.content(), request.department(), request.type());
        noticeRepository.save(notice);

        List<Student> recipients = resolveAudience(author.getUniversity().getId(), request.audience());
        for (Student student : recipients) {
            noticeReceptionRepository.save(new NoticeReception(UuidCreator.create(), notice, student));
        }
        notice.assignRecipientCount(recipients.size());

        if (request.files() != null) {
            for (String fileId : request.files()) {
                UploadedFile file = uploadedFileRepository.findById(fileId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
                noticeFileRepository.save(new NoticeFile(UuidCreator.create(), notice, file));
            }
        }

        return new CreateNoticeResponse(notice.getId(), notice.getTotalRecipientCount(), notice.getCreatedAt());
    }

    public Page<AdminNoticeSummaryResponse> getSentNotices(String staffId, String department, String keyword,
                                                            int page, int size) {
        Staff staff = staffService.getStaff(staffId);
        Page<Notice> notices = noticeRepository.searchForAdmin(staff.getUniversity().getId(), department, keyword,
                PageRequest.of(Math.max(page - 1, 0), size));
        return notices.map(notice -> AdminNoticeSummaryResponse.of(notice,
                noticeReceptionRepository.countByNoticeIdAndReadTrue(notice.getId())));
    }

    public AdminNoticeDetailResponse getNoticeDetail(String noticeId) {
        Notice notice = getNotice(noticeId);
        long readCount = noticeReceptionRepository.countByNoticeIdAndReadTrue(noticeId);
        List<NoticeFileResponse> files = noticeFileRepository.findByNoticeId(noticeId).stream()
                .map(NoticeFileResponse::from)
                .toList();
        return AdminNoticeDetailResponse.of(notice, readCount, files);
    }

    public List<NoticeReceptionResponse> getReceptions(String noticeId) {
        getNotice(noticeId);
        return noticeReceptionRepository.findByNoticeId(noticeId).stream()
                .map(NoticeReceptionResponse::from)
                .toList();
    }

    @Transactional
    public void resend(String noticeId) {
        Notice notice = getNotice(noticeId);
        noticeReceptionRepository.findByNoticeId(notice.getId()).forEach(NoticeReception::resetRead);
    }

    @Transactional
    public void delete(String noticeId) {
        Notice notice = getNotice(noticeId);
        noticeReceptionRepository.deleteByNoticeId(notice.getId());
        noticeFileRepository.deleteByNoticeId(notice.getId());
        noticeRepository.delete(notice);
    }

    public Notice getNotice(String noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }

    private List<Student> resolveAudience(String universityId, AudienceRequest audience) {
        return switch (audience.mode()) {
            case ALL -> studentRepository.findAllByUniversityId(universityId);
            case GROUP -> {
                List<String> upperKeys = audience.groupKeys() == null ? List.of()
                        : audience.groupKeys().stream().map(k -> k.toUpperCase(Locale.ROOT)).toList();
                yield studentRepository.findAllByUniversityIdAndCountryIn(universityId, upperKeys);
            }
            case INDIVIDUAL -> audience.studentIds() == null ? List.of()
                    : studentRepository.findAllByIdIn(audience.studentIds());
        };
    }
}
