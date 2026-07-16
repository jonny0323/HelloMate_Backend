package com.HelloMate.HelloMateBackend.domain.honeytip.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "honey_tip_edit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoneyTipEdit extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "honey_tip_id", nullable = false)
    private HoneyTip honeyTip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Student requester;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EditRequestStatus status;

    private LocalDateTime reviewedAt;

    public HoneyTipEdit(String id, HoneyTip honeyTip, Student requester, String content) {
        this.id = id;
        this.honeyTip = honeyTip;
        this.requester = requester;
        this.content = content;
        this.status = EditRequestStatus.PENDING;
    }

    public void review(EditRequestStatus status) {
        this.status = status;
        this.reviewedAt = LocalDateTime.now();
    }
}
