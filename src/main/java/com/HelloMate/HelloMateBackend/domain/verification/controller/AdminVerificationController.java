package com.HelloMate.HelloMateBackend.domain.verification.controller;

import com.HelloMate.HelloMateBackend.domain.verification.dto.request.ReviewVerificationDocumentRequest;
import com.HelloMate.HelloMateBackend.domain.verification.dto.response.VerificationDocumentResponse;
import com.HelloMate.HelloMateBackend.domain.verification.entity.VerificationStatus;
import com.HelloMate.HelloMateBackend.domain.verification.service.VerificationService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.common.response.PageMeta;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/verification-documents")
@RequiredArgsConstructor
public class AdminVerificationController {

    private final VerificationService verificationService;

    @GetMapping
    public ApiResponse<List<VerificationDocumentResponse>> getDocuments(@RequestParam(required = false) VerificationStatus status,
                                                                         @RequestParam(defaultValue = "1") int page,
                                                                         @RequestParam(defaultValue = "20") int size) {
        Page<VerificationDocumentResponse> result = verificationService.getDocuments(status, page, size);
        return ApiResponse.ok(result.getContent(), new PageMeta(page, size, result.getTotalElements()));
    }

    @PatchMapping("/{documentId}")
    public ApiResponse<VerificationDocumentResponse> review(@CurrentUser AuthPrincipal principal,
                                                             @PathVariable String documentId,
                                                             @Valid @RequestBody ReviewVerificationDocumentRequest request) {
        return ApiResponse.ok(verificationService.review(principal.id(), documentId, request));
    }
}
