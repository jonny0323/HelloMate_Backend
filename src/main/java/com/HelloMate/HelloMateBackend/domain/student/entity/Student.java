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

/**
 * ERD의 users 테이블. 로그인 식별자로 email을 추가했다 (ERD/설계 문서에는 학생 로그인 식별자가
 * 명시되어 있지 않아, 담당자(선생님)에 추가된 email 패턴과 동일하게 보완함).
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseTimeEntity {

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

    public Student(String id, University university, String email, String name, String password, String country,
                   String language, StudentType studentType, String major, String grade) {
        this.id = id;
        this.university = university;
        this.email = email;
        this.name = name;
        this.password = password;
        this.country = country;
        this.language = language;
        this.studentType = studentType;
        this.major = major;
        this.grade = grade;
    }

    public void updateProfile(String language, String major, String grade) {
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
}
