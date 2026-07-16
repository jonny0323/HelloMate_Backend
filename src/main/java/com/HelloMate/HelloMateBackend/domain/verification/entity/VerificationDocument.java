package com.HelloMate.HelloMateBackend.domain.verification.entity;

import com.HelloMate.HelloMateBackend.domain.file.entity.UploadedFile;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 설계 문서 8장 갭 보완: 재학 인증 서류 승인 플로우.
 */
@Entity
@Table(name = "verification_document")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationDocument extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private UploadedFile file;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_staff_id")
    private Staff reviewedBy;

    private LocalDateTime reviewedAt;

    public VerificationDocument(String id, Student student, UploadedFile file) {
        this.id = id;
        this.student = student;
        this.file = file;
        this.status = VerificationStatus.PENDING;
    }

    public void review(VerificationStatus status, Staff reviewer) {
        this.status = status;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }
}
