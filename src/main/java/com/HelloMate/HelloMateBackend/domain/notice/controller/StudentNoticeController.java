package com.HelloMate.HelloMateBackend.domain.notice.controller;

import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeFileResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.StudentNoticeDetailResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.UnreadCountResponse;
import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeReception;
import com.HelloMate.HelloMateBackend.domain.notice.service.NoticeQueryService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class StudentNoticeController {

    private final NoticeQueryService noticeQueryService;

    @GetMapping
    public ApiResponse<Object> getMyNotices(@CurrentUser AuthPrincipal principal,
                                             @RequestParam(required = false) String groupBy,
                                             @RequestParam(required = false) String q,
                                             @RequestParam(required = false) String cursor,
                                             @RequestParam(defaultValue = "20") int limit) {
        Slice<NoticeReception> slice = noticeQueryService.getMyNoticeSlice(principal.id(), q, cursor, limit);
        return ApiResponse.ok(noticeQueryService.toResponseData(slice, groupBy), noticeQueryService.cursorMetaOf(slice));
    }

    @GetMapping("/{noticeId}")
    public ApiResponse<StudentNoticeDetailResponse> getNoticeDetail(@CurrentUser AuthPrincipal principal,
                                                                     @PathVariable String noticeId,
                                                                     @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
        return ApiResponse.ok(noticeQueryService.getNoticeDetailAndMarkRead(principal.id(), noticeId, acceptLanguage));
    }

    @PatchMapping("/{noticeId}/read")
    public ApiResponse<Void> markRead(@CurrentUser AuthPrincipal principal, @PathVariable String noticeId) {
        noticeQueryService.markRead(principal.id(), noticeId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(noticeQueryService.getUnreadCount(principal.id()));
    }

    @GetMapping("/{noticeId}/files")
    public ApiResponse<List<NoticeFileResponse>> getFiles(@CurrentUser AuthPrincipal principal, @PathVariable String noticeId) {
        return ApiResponse.ok(noticeQueryService.getFiles(principal.id(), noticeId));
    }
}
