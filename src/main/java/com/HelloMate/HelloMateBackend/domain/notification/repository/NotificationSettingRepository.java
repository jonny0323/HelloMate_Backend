package com.HelloMate.HelloMateBackend.domain.notification.repository;

import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, String> {

    List<NotificationSetting> findByStudentId(String studentId);

    Optional<NotificationSetting> findByStudentIdAndCategory(String studentId, NotificationCategory category);

    /** 공지 팬아웃에서 수신자별 설정을 한 번에 읽기 위한 것. */
    @EntityGraph(attributePaths = "student")
    List<NotificationSetting> findByStudentIdInAndCategory(List<String> studentIds, NotificationCategory category);
}
