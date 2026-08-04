package com.HelloMate.HelloMateBackend.domain.notification.controller;

import com.HelloMate.HelloMateBackend.domain.notification.dto.request.UpdateNotificationSettingsRequest;
import com.HelloMate.HelloMateBackend.domain.notification.dto.response.NotificationResponse;
import com.HelloMate.HelloMateBackend.domain.notification.dto.response.NotificationSettingResponse;
import com.HelloMate.HelloMateBackend.domain.notification.dto.response.NotificationUnreadCountResponse;
import com.HelloMate.HelloMateBackend.domain.notification.entity.Notification;
import com.HelloMate.HelloMateBackend.domain.notification.service.NotificationService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getFeed(@CurrentUser AuthPrincipal principal,
                                                             @RequestParam(required = false) String category,
                                                             @RequestParam(required = false) String cursor,
                                                             @RequestParam(defaultValue = "20") int limit) {
        Slice<Notification> slice = notificationService.getFeedSlice(principal.id(), category, cursor, limit);
        return ApiResponse.ok(notificationService.toResponseList(slice), notificationService.cursorMetaOf(slice));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markRead(@CurrentUser AuthPrincipal principal, @PathVariable String notificationId) {
        notificationService.markRead(principal.id(), notificationId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/unread-count")
    public ApiResponse<NotificationUnreadCountResponse> getUnreadCount(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(notificationService.getUnreadCount(principal.id()));
    }

    @GetMapping("/settings")
    public ApiResponse<List<NotificationSettingResponse>> getSettings(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(notificationService.getSettings(principal.id()));
    }

    @PatchMapping("/settings")
    public ApiResponse<List<NotificationSettingResponse>> updateSettings(@CurrentUser AuthPrincipal principal,
                                                                          @Valid @RequestBody UpdateNotificationSettingsRequest request) {
        notificationService.updateSettings(principal.id(), request);
        return ApiResponse.ok(notificationService.getSettings(principal.id()));
    }
}
