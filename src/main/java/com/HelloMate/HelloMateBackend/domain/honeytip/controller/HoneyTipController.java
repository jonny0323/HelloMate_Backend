package com.HelloMate.HelloMateBackend.domain.honeytip.controller;

import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.CreateEditRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipResponse;
import com.HelloMate.HelloMateBackend.domain.honeytip.service.HoneyTipService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/honey-tips")
@RequiredArgsConstructor
public class HoneyTipController {

    private final HoneyTipService honeyTipService;

    @GetMapping
    public ApiResponse<List<HoneyTipResponse>> getHoneyTips(@CurrentUser AuthPrincipal principal,
                                                             @RequestParam(required = false) String category) {
        return ApiResponse.ok(honeyTipService.getHoneyTips(principal.id(), category));
    }

    @GetMapping("/{honeyTipId}")
    public ApiResponse<HoneyTipResponse> getHoneyTipDetail(@PathVariable String honeyTipId) {
        return ApiResponse.ok(honeyTipService.getHoneyTipDetail(honeyTipId));
    }

    @PostMapping("/{honeyTipId}/edit-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> submitEditRequest(@CurrentUser AuthPrincipal principal, @PathVariable String honeyTipId,
                                                @Valid @RequestBody CreateEditRequest request) {
        honeyTipService.submitEditRequest(principal.id(), honeyTipId, request);
        return ApiResponse.ok(null);
    }
}
