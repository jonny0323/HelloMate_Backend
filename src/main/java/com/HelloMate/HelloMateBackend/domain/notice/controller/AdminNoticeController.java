package com.HelloMate.HelloMateBackend.domain.notice.controller;

import com.HelloMate.HelloMateBackend.domain.notice.dto.request.AudienceRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.request.CreateNoticeRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.request.SaveDraftRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.request.UpdateNoticeRequest;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AdminNoticeDetailResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AdminNoticeSummaryResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.AudienceCountResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.CreateNoticeResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.DraftResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.NoticeReceptionResponse;
import com.HelloMate.HelloMateBackend.domain.notice.dto.response.ResendResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
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

    /** 발송 전 실제 수신자 수 확인 — 화면의 "N명에게 발송됩니다" 문구용. */
    @PostMapping("/audience/count")
    public ApiResponse<AudienceCountResponse> countAudience(@CurrentUser AuthPrincipal principal,
                                                             @Valid @RequestBody AudienceRequest request) {
        return ApiResponse.ok(noticeService.countAudience(principal.id(), request));
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

    @GetMapping("/drafts")
    public ApiResponse<List<DraftResponse>> getMyDrafts(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(noticeService.getMyDrafts(principal.id()));
    }

    @PostMapping("/drafts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DraftResponse> saveDraft(@CurrentUser AuthPrincipal principal,
                                                 @Valid @RequestBody SaveDraftRequest request) {
        return ApiResponse.ok(noticeService.saveDraft(principal.id(), request));
    }

    @PatchMapping("/drafts/{noticeId}")
    public ApiResponse<DraftResponse> updateDraft(@CurrentUser AuthPrincipal principal,
                                                   @PathVariable String noticeId,
                                                   @Valid @RequestBody SaveDraftRequest request) {
        return ApiResponse.ok(noticeService.updateDraft(principal.id(), noticeId, request));
    }

    @PostMapping("/drafts/{noticeId}/send")
    public ApiResponse<CreateNoticeResponse> sendDraft(@CurrentUser AuthPrincipal principal,
                                                        @PathVariable String noticeId,
                                                        @Valid @RequestBody CreateNoticeRequest request) {
        return ApiResponse.ok(noticeService.sendDraft(principal.id(), noticeId, request));
    }

    @GetMapping("/{noticeId}")
    public ApiResponse<AdminNoticeDetailResponse> getNoticeDetail(@CurrentUser AuthPrincipal principal,
                                                                   @PathVariable String noticeId) {
        return ApiResponse.ok(noticeService.getNoticeDetail(principal.id(), noticeId));
    }

    @PatchMapping("/{noticeId}")
    public ApiResponse<AdminNoticeDetailResponse> update(@CurrentUser AuthPrincipal principal,
                                                          @PathVariable String noticeId,
                                                          @RequestBody UpdateNoticeRequest request) {
        return ApiResponse.ok(noticeService.update(principal.id(), noticeId, request));
    }

    @GetMapping("/{noticeId}/receptions")
    public ApiResponse<List<NoticeReceptionResponse>> getReceptions(@CurrentUser AuthPrincipal principal,
                                                                     @PathVariable String noticeId,
                                                                     @RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "50") int size) {
        Page<NoticeReceptionResponse> result = noticeService.getReceptions(principal.id(), noticeId, page, size);
        return ApiResponse.ok(result.getContent(), new PageMeta(page, size, result.getTotalElements()));
    }

    @PostMapping("/{noticeId}/resend")
    public ApiResponse<ResendResponse> resend(@CurrentUser AuthPrincipal principal, @PathVariable String noticeId) {
        return ApiResponse.ok(new ResendResponse(noticeService.resend(principal.id(), noticeId)));
    }

    @DeleteMapping("/{noticeId}")
    public ApiResponse<Void> delete(@CurrentUser AuthPrincipal principal, @PathVariable String noticeId) {
        noticeService.delete(principal.id(), noticeId);
        return ApiResponse.ok(null);
    }
}
