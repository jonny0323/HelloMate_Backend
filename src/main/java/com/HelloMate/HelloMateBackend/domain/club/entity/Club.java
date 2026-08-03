package com.HelloMate.HelloMateBackend.domain.club.entity;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
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

import java.time.LocalDate;

/**
 * club.members(현재 인원수)는 실제 멤버 목록이 아니라 club_member로 집계되는 캐시 카운트다
 * (설계 문서 5장 메모). 실제 소스 오브 트루스는 {@link ClubMember}.
 */
@Entity
@Table(name = "club")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Club extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Student creator;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String introduction;

    @Column(nullable = false)
    private int maxMembers;

    @Column(nullable = false)
    private int currentMembers;

    @Column(nullable = false)
    private LocalDate deadline;

    public Club(String id, University university, Student creator, String title, String introduction,
                int maxMembers, LocalDate deadline) {
        this.id = id;
        this.university = university;
        this.creator = creator;
        this.title = title;
        this.introduction = introduction;
        this.maxMembers = maxMembers;
        this.currentMembers = 0;
        this.deadline = deadline;
    }

    public boolean isFull() {
        return currentMembers >= maxMembers;
    }

    public void increaseMember() {
        this.currentMembers++;
    }

    public void decreaseMember() {
        this.currentMembers = Math.max(0, this.currentMembers - 1);
    }

    public void updateInfo(String title, String introduction, Integer maxMembers, LocalDate deadline) {
        if (title != null) {
            this.title = title;
        }
        if (introduction != null) {
            this.introduction = introduction;
        }
        if (maxMembers != null) {
            this.maxMembers = maxMembers;
        }
        if (deadline != null) {
            this.deadline = deadline;
        }
    }
}
