package com.HelloMate.HelloMateBackend.domain.club.entity;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.university.entity.University;
import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
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

    /** deadline은 날짜 단위라 그날 하루는 열려 있는 것으로 본다(마감 판정은 다음 날 00:00부터). */
    public boolean isDeadlinePassed() {
        return deadline.isBefore(LocalDate.now());
    }

    /** 정원이 남아 있어도 마감일이 지났으면 더 이상 모집하지 않는다. */
    public boolean isRecruitClosed() {
        return isFull() || isDeadlinePassed();
    }

    public ClubCardState resolveCardState(boolean joined) {
        if (joined) {
            return ClubCardState.JOINED;
        }
        return isRecruitClosed() ? ClubCardState.CLOSED : ClubCardState.JOINABLE;
    }

    public int remainingSeats() {
        return Math.max(0, maxMembers - currentMembers);
    }

    /** 정원/마감 검증을 엔티티 안에 두어 어느 경로로 참여하든 같은 규칙을 타게 한다. */
    public void join() {
        if (isDeadlinePassed()) {
            throw new BusinessException(ErrorCode.CLUB_RECRUIT_CLOSED);
        }
        if (isFull()) {
            throw new BusinessException(ErrorCode.CLUB_FULL);
        }
        this.currentMembers++;
    }

    public void increaseMember() {
        this.currentMembers++;
    }

    public boolean isCreator(String studentId) {
        return this.creator.getId().equals(studentId);
    }

    public void changeCreator(Student newCreator) {
        this.creator = newCreator;
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
