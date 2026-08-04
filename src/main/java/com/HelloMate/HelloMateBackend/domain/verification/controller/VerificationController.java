package com.HelloMate.HelloMateBackend.domain.verification.controller;

import com.HelloMate.HelloMateBackend.domain.verification.dto.request.SubmitVerificationDocumentRequest;
import com.HelloMate.HelloMateBackend.domain.verification.dto.response.VerificationDocumentResponse;
import com.HelloMate.HelloMateBackend.domain.verification.service.VerificationService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students/me/verification-documents")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VerificationDocumentResponse> submit(@CurrentUser AuthPrincipal principal,
                                                             @Valid @RequestBody SubmitVerificationDocumentRequest request) {
        return ApiResponse.ok(verificationService.submit(principal.id(), request.fileId()));
    }

    @GetMapping
    public ApiResponse<VerificationDocumentResponse> getMyDocument(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(verificationService.getMyDocument(principal.id()));
    }
}
