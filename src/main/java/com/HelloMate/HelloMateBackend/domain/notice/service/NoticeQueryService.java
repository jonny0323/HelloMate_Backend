package com.HelloMate.HelloMateBackend.domain.notice.service;

import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeBannerResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeFileResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeHomeResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.StudentNoticeDetailResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.StudentNoticeSummaryResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.UnreadCountResponse;
import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;
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

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeQueryService {

    private static final int BANNER_LIMIT = 5;
    private static final int RECENT_NOTICE_LIMIT = 5;

    private final NoticeReceptionRepository noticeReceptionRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final TranslationService translationService;

    /** 화면: 공지사항 홈 (중요 배너 캐러셀 + 최근 공지 목록). */
    public NoticeHomeResponse getHome(String studentId) {
        List<NoticeBannerResponse> banners = noticeReceptionRepository
                .findActiveBanners(studentId, NoticeType.URGENT, LocalDate.now(), PageRequest.of(0, BANNER_LIMIT))
                .stream()
                .map(reception -> NoticeBannerResponse.from(reception.getNotice()))
                .toList();

        List<StudentNoticeSummaryResponse> recentNotices = noticeReceptionRepository
                .findByStudentIdOrderByCreatedAtDesc(studentId, null, null, PageRequest.of(0, RECENT_NOTICE_LIMIT))
                .getContent()
                .stream()
                .map(StudentNoticeSummaryResponse::from)
                .toList();

        return new NoticeHomeResponse(banners, recentNotices);
    }

    public Slice<NoticeReception> getMyNoticeSlice(String studentId, String keyword, String cursor, int limit) {
        return noticeReceptionRepository.findByStudentIdOrderByCreatedAtDesc(
                studentId, keyword, CursorPageUtil.decode(cursor), PageRequest.of(0, limit));
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
