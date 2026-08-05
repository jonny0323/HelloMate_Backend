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

/**
 * ERD의 "선생님"(교직원/담당자) 테이블. department/email/isVerified는
 * 설계 문서 1.2절에서 제안된 갭 보완 컬럼이다.
 */
@Entity
@Table(name = "teacher")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Staff extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String position;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean verified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_code_id", nullable = false)
    private StaffInviteCode inviteCode;

    public Staff(String id, University university, String email, String name, String position, String department,
                 String password, StaffInviteCode inviteCode) {
        this.id = id;
        this.university = university;
        this.email = email;
        this.name = name;
        this.position = position;
        this.department = department;
        this.password = password;
        this.verified = false;
        this.inviteCode = inviteCode;
    }

    public void approve() {
        this.verified = true;
    }

    public boolean isSameUniversity(String universityId) {
        return this.university.getId().equals(universityId);
    }

    public void updateProfile(String name, String position) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (position != null && !position.isBlank()) {
            this.position = position;
        }
    }
}
