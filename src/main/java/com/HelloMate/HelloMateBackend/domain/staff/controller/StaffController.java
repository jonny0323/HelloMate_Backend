package com.HelloMate.HelloMateBackend.domain.staff.controller;

import com.HelloMate.HelloMateBackend.domain.staff.dto.request.StaffProfileUpdateRequest;
import com.HelloMate.HelloMateBackend.domain.staff.dto.response.StaffProfileResponse;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/staff/me")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    public ApiResponse<StaffProfileResponse> getMyProfile(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(staffService.getMyProfile(principal.id()));
    }

    @PatchMapping
    public ApiResponse<StaffProfileResponse> updateMyProfile(@CurrentUser AuthPrincipal principal,
                                                              @Valid @RequestBody StaffProfileUpdateRequest request) {
        return ApiResponse.ok(staffService.updateMyProfile(principal.id(), request));
    }
}
