package com.HelloMate.HelloMateBackend.domain.admin.controller;

import com.HelloMate.HelloMateBackend.domain.admin.dto.response.AdminDashboardResponse;
import com.HelloMate.HelloMateBackend.domain.admin.service.AdminDashboardService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ApiResponse<AdminDashboardResponse> getDashboard(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(adminDashboardService.getDashboard(principal.id()));
    }
}
