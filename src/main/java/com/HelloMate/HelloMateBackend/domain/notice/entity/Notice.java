package com.HelloMate.HelloMateBackend.domain.notice.entity;

import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.university.entity.University;
import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 공지는 초안(DRAFT)으로 만들어졌다가 발송(SENT)되면 수신자별 {@link NoticeReception}이 생긴다.
 * 삭제는 소프트 삭제다 — chat_thread가 notice를 FK로 참조하고 있어 물리 삭제가 불가능하고,
 * 학생이 이미 읽은 공지의 열람 이력도 남겨야 한다.
 */
@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

    /** 같은 공지를 반복해서 밀어 넣지 못하게 하는 최소 간격. */
    private static final int RESEND_COOLDOWN_HOURS = 24;

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff author;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 작성자 소속 부서를 그대로 굳힌 값. 요청으로 받지 않는다 (부서 사칭 방지). */
    @Column(nullable = false, length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeType type;

    @Column(nullable = false)
    private int totalRecipientCount;

    private LocalDate bannerStartDate;
    private LocalDate bannerEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AudienceMode audienceMode;

    /** 발송함에 그대로 노출되는 대상 설명("전체 학생", "베트남 유학생, 컴퓨터공학과" 등). */
    @Column(length = 255)
    private String audienceLabel;

    private LocalDateTime sentAt;
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private int resendCount;

    private LocalDateTime lastResentAt;

    private Notice(String id, University university, Staff author, String title, String content,
                   NoticeType type, NoticeStatus status) {
        this.id = id;
        this.university = university;
        this.author = author;
        this.title = title;
        this.content = content;
        this.department = author.getDepartment();
        this.type = type;
        this.status = status;
        this.totalRecipientCount = 0;
        this.resendCount = 0;
    }

    /**
     * 공지는 항상 DRAFT로 태어나고 {@link #markSent}로만 SENT가 된다. 즉시 발송도 마찬가지다 —
     * 생성 시점에 SENT로 만들어 두면 뒤따르는 markSent()가 자기 자신을 중복 발송으로 오인한다.
     * 제목/내용은 임시저장이면 아직 비어 있을 수 있어 빈 문자열을 허용한다(컬럼은 NOT NULL 유지).
     */
    public static Notice draft(String id, Staff author, String title, String content, NoticeType type) {
        return new Notice(id, author.getUniversity(), author,
                title == null ? "" : title, content == null ? "" : content, type, NoticeStatus.DRAFT);
    }

    public void assignBannerPeriod(LocalDate startDate, LocalDate endDate) {
        this.bannerStartDate = startDate;
        this.bannerEndDate = endDate;
    }

    public void updateContent(String title, String content, NoticeType type) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (type != null) {
            this.type = type;
        }
    }

    /** 초안을 실제 발송 상태로 확정한다. 수신자 수와 대상 스냅샷을 이 시점에 굳힌다. */
    public void markSent(int recipientCount, AudienceMode audienceMode, String audienceLabel) {
        if (this.status == NoticeStatus.SENT) {
            throw new BusinessException(ErrorCode.NOTICE_ALREADY_SENT);
        }
        this.status = NoticeStatus.SENT;
        this.totalRecipientCount = recipientCount;
        this.audienceMode = audienceMode;
        this.audienceLabel = audienceLabel;
        this.sentAt = LocalDateTime.now();
    }

    public boolean isDraft() {
        return this.status == NoticeStatus.DRAFT;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 재발송은 미열람자에게 알림만 다시 보내는 동작이다. 열람 상태를 되돌리지 않는다 —
     * 되돌리면 이미 읽은 학생의 readAt이 사라지고 열람률 통계가 왜곡된다.
     */
    public void recordResend() {
        if (this.status != NoticeStatus.SENT) {
            throw new BusinessException(ErrorCode.NOTICE_NOT_SENT);
        }
        if (lastResentAt != null && lastResentAt.plusHours(RESEND_COOLDOWN_HOURS).isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.NOTICE_RESEND_TOO_SOON);
        }
        this.resendCount++;
        this.lastResentAt = LocalDateTime.now();
    }

    public boolean isSameUniversity(String universityId) {
        return this.university.getId().equals(universityId);
    }

    public boolean isSameDepartment(String departmentName) {
        return this.department.equals(departmentName);
    }

    /** 발송함에서 이 공지의 [재발송]/[삭제] 버튼을 열어줄지 판단하는 값. */
    public boolean isManageableBy(Staff staff) {
        return isSameUniversity(staff.getUniversity().getId()) && isSameDepartment(staff.getDepartment());
    }
}
