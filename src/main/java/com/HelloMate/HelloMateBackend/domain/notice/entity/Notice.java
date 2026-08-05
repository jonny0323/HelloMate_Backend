package com.HelloMate.HelloMateBackend.domain.notice.entity;

import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.university.entity.University;
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

import java.time.LocalDate;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

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

    @Column(nullable = false, length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeType type;

    @Column(nullable = false)
    private int totalRecipientCount;

    /** 공지 홈 상단 배너 노출 기간. 비워두면 기간 제한 없이 계속 배너로 뜬다. */
    private LocalDate bannerStartDate;
    private LocalDate bannerEndDate;

    public Notice(String id, University university, Staff author, String title, String content, String department,
                  NoticeType type) {
        this.id = id;
        this.university = university;
        this.author = author;
        this.title = title;
        this.content = content;
        this.department = department;
        this.type = type;
        this.totalRecipientCount = 0;
    }

    public void assignRecipientCount(int count) {
        this.totalRecipientCount = count;
    }

    public void assignBannerPeriod(LocalDate startDate, LocalDate endDate) {
        this.bannerStartDate = startDate;
        this.bannerEndDate = endDate;
    }
}
