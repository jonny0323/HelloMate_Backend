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

    public HoneyTip(String id, University university, Staff author, String category, String title, String content) {
        this.id = id;
        this.university = university;
        this.author = author;
        this.category = category;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
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
