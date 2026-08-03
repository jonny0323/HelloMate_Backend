package com.HelloMate.HelloMateBackend.domain.notice.controller;

import com.HelloMate.HelloMateBackend.domain.notice.dto.request.CreateNoticeRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AdminNoticeDetailResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AdminNoticeSummaryResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.CreateNoticeResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeReceptionResponse;
import com.HelloMate.HelloMateBackend.domain.notice.service.NoticeService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.common.response.PageMeta;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateNoticeResponse> create(@CurrentUser AuthPrincipal principal,
                                                     @Valid @RequestBody CreateNoticeRequest request) {
        return ApiResponse.ok(noticeService.createAndSend(principal.id(), request));
    }

    @GetMapping
    public ApiResponse<List<AdminNoticeSummaryResponse>> getSentNotices(@CurrentUser AuthPrincipal principal,
                                                                         @RequestParam(required = false) String department,
                                                                         @RequestParam(required = false) String keyword,
                                                                         @RequestParam(defaultValue = "1") int page,
                                                                         @RequestParam(defaultValue = "20") int size) {
        Page<AdminNoticeSummaryResponse> result = noticeService.getSentNotices(principal.id(), department, keyword, page, size);
        return ApiResponse.ok(result.getContent(), new PageMeta(page, size, result.getTotalElements()));
    }

    @GetMapping("/{noticeId}")
    public ApiResponse<AdminNoticeDetailResponse> getNoticeDetail(@PathVariable String noticeId) {
        return ApiResponse.ok(noticeService.getNoticeDetail(noticeId));
    }

    @GetMapping("/{noticeId}/receptions")
    public ApiResponse<List<NoticeReceptionResponse>> getReceptions(@PathVariable String noticeId) {
        return ApiResponse.ok(noticeService.getReceptions(noticeId));
    }

    @PostMapping("/{noticeId}/resend")
    public ApiResponse<Void> resend(@PathVariable String noticeId) {
        noticeService.resend(noticeId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{noticeId}")
    public ApiResponse<Void> delete(@PathVariable String noticeId) {
        noticeService.delete(noticeId);
        return ApiResponse.ok(null);
    }
}
