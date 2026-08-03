package com.HelloMate.HelloMateBackend.domain.staff.entity;

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

import java.time.LocalDateTime;

/**
 * 설계 문서 1.2절 갭 보완: 부서별 1회용 담당자 초대 코드.
 */
@Entity
@Table(name = "staff_invite_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StaffInviteCode extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public StaffInviteCode(String id, University university, String department, String code, LocalDateTime expiresAt) {
        this.id = id;
        this.university = university;
        this.department = department;
        this.code = code;
        this.used = false;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public void markUsed() {
        this.used = true;
    }
}
