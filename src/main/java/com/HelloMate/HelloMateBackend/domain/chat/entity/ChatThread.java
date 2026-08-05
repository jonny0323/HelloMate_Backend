package com.HelloMate.HelloMateBackend.domain.chat.entity;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
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

import java.time.LocalDateTime;

/**
 * threadId는 (student_id, staff_id) 조합을 의미한다 (설계 문서 3장).
 * notice는 "공지 관련 문의" 흐름을 위한 nullable FK로, ERD 갭 보완 항목이다.
 */
@Entity
@Table(name = "chat_thread", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "staff_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatThread extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    @Column(nullable = false)
    private boolean studentUnread;

    @Column(nullable = false)
    private boolean staffUnread;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ThreadInitiator initiatedBy;

    public ChatThread(String id, Student student, Staff staff, Notice notice, ThreadInitiator initiatedBy) {
        this.id = id;
        this.student = student;
        this.staff = staff;
        this.notice = notice;
        this.studentUnread = false;
        this.staffUnread = false;
        this.initiatedBy = initiatedBy;
    }

    public void receiveMessage(SenderType senderType, String content, LocalDateTime sentAt) {
        this.lastMessage = content;
        this.lastMessageAt = sentAt;
        if (senderType == SenderType.USER) {
            this.staffUnread = true;
        } else {
            this.studentUnread = true;
        }
    }

    public void markReadByStudent() {
        this.studentUnread = false;
    }

    public void markReadByStaff() {
        this.staffUnread = false;
    }
}
