package com.HelloMate.HelloMateBackend.domain.honeytip.controller;

import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.CreateHoneyTipRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.ReviewEditRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.request.UpdateHoneyTipRequest;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipEditResponse;
import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipResponse;
import com.HelloMate.HelloMateBackend.domain.honeytip.service.HoneyTipService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/honey-tips")
@RequiredArgsConstructor
public class AdminHoneyTipController {

    private final HoneyTipService honeyTipService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HoneyTipResponse> createHoneyTip(@CurrentUser AuthPrincipal principal,
                                                         @Valid @RequestBody CreateHoneyTipRequest request) {
        return ApiResponse.ok(honeyTipService.createHoneyTip(principal.id(), request));
    }

    @PatchMapping("/{honeyTipId}")
    public ApiResponse<HoneyTipResponse> updateHoneyTip(@PathVariable String honeyTipId,
                                                         @RequestBody UpdateHoneyTipRequest request) {
        return ApiResponse.ok(honeyTipService.updateHoneyTip(honeyTipId, request));
    }

    @GetMapping("/{honeyTipId}/edit-requests")
    public ApiResponse<List<HoneyTipEditResponse>> getEditRequests(@PathVariable String honeyTipId) {
        return ApiResponse.ok(honeyTipService.getEditRequests(honeyTipId));
    }

    @PatchMapping("/edit-requests/{reqId}")
    public ApiResponse<HoneyTipEditResponse> reviewEditRequest(@PathVariable String reqId,
                                                                @Valid @RequestBody ReviewEditRequest request) {
        return ApiResponse.ok(honeyTipService.reviewEditRequest(reqId, request));
    }
}
