package com.HelloMate.HelloMateBackend.domain.notification.service;

import com.HelloMate.HelloMateBackend.domain.notification.dto.request.NotificationSettingUpdateItem;
import com.HelloMate.HelloMateBackend.domain.notification.dto.request.UpdateNotificationSettingsRequest;
import com.HelloMate.HelloMateBackend.domain.notification.dto.response.NotificationResponse;
import com.HelloMate.HelloMateBackend.domain.notification.dto.response.NotificationSettingResponse;
import com.HelloMate.HelloMateBackend.domain.notification.dto.response.NotificationUnreadCountResponse;
import com.HelloMate.HelloMateBackend.domain.notification.entity.Notification;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationSetting;
import com.HelloMate.HelloMateBackend.domain.notification.repository.NotificationRepository;
import com.HelloMate.HelloMateBackend.domain.notification.repository.NotificationSettingRepository;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.response.CursorMeta;
import com.HelloMate.HelloMateBackend.global.common.util.CursorPageUtil;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * notify()가 다른 도메인 서비스(Notice/Post/Club/HoneyTip)가 알림을 발행하는 진입점이다
 * (설계: docs/roadmaps/notification.md).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final StudentService studentService;

    @Transactional
    public void notify(Student recipient, NotificationCategory category, String title, String linkType, String linkId) {
        boolean enabled = notificationSettingRepository.findByStudentIdAndCategory(recipient.getId(), category)
                .map(NotificationSetting::isEnabled)
                .orElse(true);
        if (!enabled) {
            return;
        }
        notificationRepository.save(new Notification(UuidCreator.create(), recipient, category, title, linkType, linkId));
    }

    public Slice<Notification> getFeedSlice(String studentId, String categoryParam, String cursor, int limit) {
        NotificationCategory category = categoryParam == null ? null : NotificationCategory.from(categoryParam);
        return notificationRepository.findFeed(studentId, category, CursorPageUtil.decode(cursor), PageRequest.of(0, limit));
    }

    public List<NotificationResponse> toResponseList(Slice<Notification> slice) {
        return slice.getContent().stream().map(NotificationResponse::from).toList();
    }

    public CursorMeta cursorMetaOf(Slice<Notification> slice) {
        String nextCursor = slice.hasNext() && !slice.getContent().isEmpty()
                ? CursorPageUtil.encode(slice.getContent().get(slice.getContent().size() - 1).getCreatedAt())
                : null;
        return new CursorMeta(nextCursor, slice.hasNext(), slice.getContent().size());
    }

    @Transactional
    public int markAllRead(String studentId) {
        return notificationRepository.markAllRead(studentId, LocalDateTime.now());
    }

    @Transactional
    public void markRead(String studentId, String notificationId) {
        Notification notification = notificationRepository.findByIdAndStudentId(notificationId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markRead();
    }

    public NotificationUnreadCountResponse getUnreadCount(String studentId) {
        return new NotificationUnreadCountResponse(notificationRepository.countByStudentIdAndReadFalse(studentId));
    }

    public List<NotificationSettingResponse> getSettings(String studentId) {
        Map<NotificationCategory, Boolean> overrides = notificationSettingRepository.findByStudentId(studentId).stream()
                .collect(Collectors.toMap(NotificationSetting::getCategory, NotificationSetting::isEnabled));
        return Arrays.stream(NotificationCategory.values())
                .map(category -> new NotificationSettingResponse(category, overrides.getOrDefault(category, true),
                        category.isRequired()))
                .toList();
    }

    @Transactional
    public void updateSettings(String studentId, UpdateNotificationSettingsRequest request) {
        Student student = studentService.getStudent(studentId);
        for (NotificationSettingUpdateItem item : request.settings()) {
            if (item.category().isRequired() && !item.enabled()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "시스템 안내는 끌 수 없습니다.");
            }
            NotificationSetting setting = notificationSettingRepository
                    .findByStudentIdAndCategory(studentId, item.category())
                    .orElseGet(() -> notificationSettingRepository.save(
                            new NotificationSetting(UuidCreator.create(), student, item.category(), true)));
            setting.updateEnabled(item.enabled());
        }
    }
}
