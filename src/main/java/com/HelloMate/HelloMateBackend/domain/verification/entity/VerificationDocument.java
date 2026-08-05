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
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status;

    /**
     * 반려 사유. 디자인의 '서류 인증 실패' 화면에는 표시 영역이 없지만, 사유 없이 반려하면
     * 학생이 같은 서류를 그대로 다시 올리는 루프에 빠진다.
     */
    @Column(length = 500)
    private String rejectReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_staff_id")
    private Staff reviewedBy;

    private LocalDateTime reviewedAt;

    public VerificationDocument(String id, Student student, UploadedFile file, DocumentType documentType) {
        this.id = id;
        this.student = student;
        this.file = file;
        this.documentType = documentType;
        this.status = VerificationStatus.PENDING;
    }

    public void approve(Staff reviewer) {
        this.status = VerificationStatus.APPROVED;
        this.rejectReason = null;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(Staff reviewer, String rejectReason) {
        this.status = VerificationStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }
}
