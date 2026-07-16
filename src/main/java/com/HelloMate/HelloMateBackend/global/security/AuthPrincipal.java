package com.HelloMate.HelloMateBackend.global.security;

/**
 * JWT에서 추출한 인증 주체. role로 학생/담당자를 구분한다 (설계 문서 0.2절).
 */
public record AuthPrincipal(String id, Role role) {

    public boolean isStudent() {
        return role == Role.STUDENT;
    }

    public boolean isStaff() {
        return role == Role.STAFF;
    }
}
