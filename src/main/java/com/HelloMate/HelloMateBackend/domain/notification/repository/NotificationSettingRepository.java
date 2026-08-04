package com.HelloMate.HelloMateBackend.domain.notification.repository;

import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, String> {

    List<NotificationSetting> findByStudentId(String studentId);

    Optional<NotificationSetting> findByStudentIdAndCategory(String studentId, NotificationCategory category);
}
