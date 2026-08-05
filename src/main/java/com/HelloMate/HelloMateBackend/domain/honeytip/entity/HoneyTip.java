package com.HelloMate.HelloMateBackend.domain.honeytip.entity;

import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.university.entity.University;
import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "honey_tip")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoneyTip extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff author;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount;

    /** 정보글 상세 상단의 ℹ️ 하이라이트 문구. */
    @Column(length = 300)
    private String tipMessage;

    /**
     * 번호가 매겨진 STEP 목록. 관리자가 통째로 저장/수정하고 서버는 조회 시 파싱만 하며
     * STEP 단위로 질의할 일이 없어서 별도 테이블 대신 JSON 문자열로 담는다.
     */
    @Column(columnDefinition = "TEXT")
    private String stepsJson;

    @Column(length = 50)
    private String estimatedFee;

    @Column(length = 50)
    private String processingPeriod;

    @Column(length = 500)
    private String externalLink;

    public HoneyTip(String id, University university, Staff author, String category, String title, String content) {
        this.id = id;
        this.university = university;
        this.author = author;
        this.category = category;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
    }

    public void updateGuide(String tipMessage, String stepsJson, String estimatedFee, String processingPeriod,
                             String externalLink) {
        this.tipMessage = tipMessage;
        this.stepsJson = stepsJson;
        this.estimatedFee = estimatedFee;
        this.processingPeriod = processingPeriod;
        this.externalLink = externalLink;
    }

    public void increaseView() {
        this.viewCount++;
    }

    public void updateContent(String category, String title, String content) {
        if (category != null) {
            this.category = category;
        }
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }
}
