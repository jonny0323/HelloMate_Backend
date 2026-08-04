package com.HelloMate.HelloMateBackend.domain.notification.entity;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커스터마이즈한 카테고리만 행이 생긴다(opt-out 모델) — 행이 없으면 기본값 true(수신)로 취급한다.
 * 가입 시점에 카테고리 7개를 미리 만들어둘 필요가 없다.
 */
@Entity
@Table(name = "notification_setting", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "category"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationCategory category;

    @Column(nullable = false)
    private boolean enabled;

    public NotificationSetting(String id, Student student, NotificationCategory category, boolean enabled) {
        this.id = id;
        this.student = student;
        this.category = category;
        this.enabled = enabled;
    }

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
