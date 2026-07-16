package com.HelloMate.HelloMateBackend.domain.notice.service;

import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeFileResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.StudentNoticeDetailResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.StudentNoticeSummaryResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.UnreadCountResponse;
import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeFileRepository;
import com.HelloMate.HelloMateBackend.domain.notice.repository.NoticeReceptionRepository;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslatedContent;
import com.HelloMate.HelloMateBackend.domain.translation.entity.TranslationContentType;
import com.HelloMate.HelloMateBackend.domain.translation.service.TranslationService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.response.CursorMeta;
import com.HelloMate.HelloMateBackend.global.common.util.AcceptLanguageUtil;
import com.HelloMate.HelloMateBackend.global.common.util.CursorPageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeQueryService {

    private final NoticeReceptionRepository noticeReceptionRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final TranslationService translationService;

    public Slice<NoticeReception> getMyNoticeSlice(String studentId, String cursor, int limit) {
        return noticeReceptionRepository.findByStudentIdOrderByCreatedAtDesc(
                studentId, CursorPageUtil.decode(cursor), PageRequest.of(0, limit));
    }

    public Object toResponseData(Slice<NoticeReception> slice, String groupBy) {
        List<StudentNoticeSummaryResponse> items = slice.getContent().stream()
                .map(StudentNoticeSummaryResponse::from)
                .toList();

        if ("department".equals(groupBy)) {
            return items.stream()
                    .collect(Collectors.groupingBy(StudentNoticeSummaryResponse::department, LinkedHashMap::new, Collectors.toList()));
        }
        return items;
    }

    public CursorMeta cursorMetaOf(Slice<NoticeReception> slice) {
        String nextCursor = slice.hasNext() && !slice.getContent().isEmpty()
                ? CursorPageUtil.encode(slice.getContent().get(slice.getContent().size() - 1).getCreatedAt())
                : null;
        return new CursorMeta(nextCursor, slice.hasNext(), slice.getContent().size());
    }

    @Transactional
    public StudentNoticeDetailResponse getNoticeDetailAndMarkRead(String studentId, String noticeId, String acceptLanguageHeader) {
        NoticeReception reception = getReception(studentId, noticeId);
        reception.markRead();

        Notice notice = reception.getNotice();
        List<NoticeFileResponse> files = noticeFileRepository.findByNoticeId(noticeId).stream()
                .map(NoticeFileResponse::from)
                .toList();

        String targetLang = AcceptLanguageUtil.primaryLanguage(acceptLanguageHeader);
        TranslatedContent translated = targetLang == null ? null
                : translationService.getOrTranslate(TranslationContentType.NOTICE, noticeId, notice.getContent(),
                        "ko", targetLang).orElse(null);

        return StudentNoticeDetailResponse.of(notice, true, files, translated);
    }

    @Transactional
    public void markRead(String studentId, String noticeId) {
        getReception(studentId, noticeId).markRead();
    }

    public UnreadCountResponse getUnreadCount(String studentId) {
        return new UnreadCountResponse(noticeReceptionRepository.countByStudentIdAndReadFalse(studentId));
    }

    public List<NoticeFileResponse> getFiles(String studentId, String noticeId) {
        getReception(studentId, noticeId);
        return noticeFileRepository.findByNoticeId(noticeId).stream()
                .map(NoticeFileResponse::from)
                .toList();
    }

    private NoticeReception getReception(String studentId, String noticeId) {
        return noticeReceptionRepository.findByNoticeIdAndStudentId(noticeId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
