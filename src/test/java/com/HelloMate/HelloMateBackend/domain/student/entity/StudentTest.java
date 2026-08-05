package com.HelloMate.HelloMateBackend.domain.student.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudentTest {

    private Student student() {
        return new Student("student-1", null, "hellomate_student", "student@inu.ac.kr", "김지수",
                "encoded", "CN", "zh", StudentType.DEGREE_STUDENT, "컴퓨터공학부", "1학년", 2001);
    }

    @Test
    @DisplayName("가입 직후에는 학생 인증 전 상태다")
    void initialState() {
        Student student = student();

        assertThat(student.getVerificationStatus()).isEqualTo(StudentVerificationStatus.REGISTERED);
        assertThat(student.isVerified()).isFalse();
        assertThat(student.getStatus()).isEqualTo(StudentStatus.ACTIVE);
    }

    @Test
    @DisplayName("서류 제출 → 반려 → 재제출 → 승인 순으로 상태가 전이된다")
    void documentVerificationFlow() {
        Student student = student();

        student.submitVerificationDocument();
        assertThat(student.getVerificationStatus()).isEqualTo(StudentVerificationStatus.DOC_PENDING);

        student.rejectVerificationDocument();
        assertThat(student.getVerificationStatus()).isEqualTo(StudentVerificationStatus.DOC_REJECTED);
        assertThat(student.isVerified()).isFalse();

        student.submitVerificationDocument();
        student.approveVerificationDocument();
        assertThat(student.isVerified()).isTrue();
        assertThat(student.getVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일 인증만으로도 인증 완료 상태가 된다")
    void verifyByEmail() {
        Student student = student();
        student.verifyByEmail();

        assertThat(student.isVerified()).isTrue();
    }

    @Test
    @DisplayName("로그인 5회 실패에서 계정이 잠긴다")
    void lockAfterFiveFailures() {
        Student student = student();

        for (int i = 0; i < 4; i++) {
            student.increaseLoginFailCount();
            assertThat(student.isLocked()).isFalse();
        }
        student.increaseLoginFailCount();
        assertThat(student.isLocked()).isTrue();
    }

    @Test
    @DisplayName("비밀번호를 바꾸면 실패 카운트와 잠금이 함께 풀린다")
    void unlockOnPasswordChange() {
        Student student = student();
        for (int i = 0; i < 5; i++) {
            student.increaseLoginFailCount();
        }

        student.updatePassword("new-encoded");

        assertThat(student.isLocked()).isFalse();
        assertThat(student.getLoginFailCount()).isZero();
    }

    @Test
    @DisplayName("탈퇴하면 행은 남고 상태만 바뀐다")
    void withdraw() {
        Student student = student();
        student.withdraw();

        assertThat(student.isWithdrawn()).isTrue();
        assertThat(student.getWithdrawnAt()).isNotNull();
    }
}
