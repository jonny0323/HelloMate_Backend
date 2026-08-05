package com.HelloMate.HelloMateBackend.domain.student.entity;

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

import java.time.LocalDateTime;

/**
 * ERD의 users 테이블. 로그인 식별자는 디자인(로그인 화면 '아이디' 필드 + 회원가입 중복 확인 버튼)에
 * 맞춰 loginId를 쓰고, email은 학교 이메일 인증/비밀번호 찾기용 연락 수단으로 남긴다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseTimeEntity {

    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 10;

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(nullable = false, unique = true, length = 30)
    private String loginId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    private String country;

    @Column(nullable = false, length = 10)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_type", nullable = false, length = 30)
    private StudentType studentType;

    @Column(length = 100)
    private String major;

    @Column(length = 20)
    private String grade;

    private Integer birthYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentVerificationStatus verificationStatus;

    private LocalDateTime verifiedAt;
    private LocalDateTime withdrawnAt;
    private LocalDateTime termsAgreedAt;
    private LocalDateTime privacyAgreedAt;

    @Column(nullable = false)
    private int loginFailCount;

    private LocalDateTime lockedUntil;

    public Student(String id, University university, String loginId, String email, String name, String password,
                   String country, String language, StudentType studentType, String major, String grade,
                   Integer birthYear) {
        this.id = id;
        this.university = university;
        this.loginId = loginId;
        this.email = email;
        this.name = name;
        this.password = password;
        this.country = country;
        this.language = language;
        this.studentType = studentType;
        this.major = major;
        this.grade = grade;
        this.birthYear = birthYear;
        this.status = StudentStatus.ACTIVE;
        this.verificationStatus = StudentVerificationStatus.REGISTERED;
        this.loginFailCount = 0;
    }

    public void agreeTerms() {
        LocalDateTime now = LocalDateTime.now();
        this.termsAgreedAt = now;
        this.privacyAgreedAt = now;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        unlock();
    }

    public void updateProfile(String name, String country, Integer birthYear, String language, String major, String grade) {
        if (name != null) {
            this.name = name;
        }
        if (country != null) {
            this.country = country;
        }
        if (birthYear != null) {
            this.birthYear = birthYear;
        }
        if (language != null) {
            this.language = language;
        }
        if (major != null) {
            this.major = major;
        }
        if (grade != null) {
            this.grade = grade;
        }
    }

    /* ===== 학생 인증 상태 전이 ===== */

    public void verifyByEmail() {
        this.verificationStatus = StudentVerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void submitVerificationDocument() {
        this.verificationStatus = StudentVerificationStatus.DOC_PENDING;
    }

    public void approveVerificationDocument() {
        this.verificationStatus = StudentVerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void rejectVerificationDocument() {
        this.verificationStatus = StudentVerificationStatus.DOC_REJECTED;
    }

    public boolean isVerified() {
        return this.verificationStatus == StudentVerificationStatus.VERIFIED;
    }

    /* ===== 계정 상태 ===== */

    public boolean isWithdrawn() {
        return this.status == StudentStatus.WITHDRAWN;
    }

    public void withdraw() {
        this.status = StudentStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    /* ===== 로그인 실패 잠금 ===== */

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    /** 5회째 실패에서 잠그고 카운터를 되돌린다 — 잠금 해제 후 1회 실패로 다시 잠기지 않게 하려는 것. */
    public void increaseLoginFailCount() {
        this.loginFailCount++;
        if (this.loginFailCount >= MAX_LOGIN_FAIL_COUNT) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
            this.loginFailCount = 0;
        }
    }

    public void unlock() {
        this.loginFailCount = 0;
        this.lockedUntil = null;
    }
}
