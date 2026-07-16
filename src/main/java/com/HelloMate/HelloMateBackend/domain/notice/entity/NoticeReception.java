package com.HelloMate.HelloMateBackend.domain.notice.entity;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice_reception", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"notice_id", "student_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeReception extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private boolean read;

    private LocalDateTime readAt;

    public NoticeReception(String id, Notice notice, Student student) {
        this.id = id;
        this.notice = notice;
        this.student = student;
        this.read = false;
    }

    public void markRead() {
        if (!this.read) {
            this.read = true;
            this.readAt = LocalDateTime.now();
        }
    }

    public void resetRead() {
        this.read = false;
        this.readAt = null;
    }
}
