package com.HelloMate.HelloMateBackend.domain.auth.entity;

import com.HelloMate.HelloMateBackend.global.security.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * subject(student/staff id) + role 조합으로 리프레시 토큰을 저장해 재발급/로그아웃(폐기)을 관리한다.
 */
@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String subjectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, length = 512, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public RefreshToken(String subjectId, Role role, String token, LocalDateTime expiresAt) {
        this.subjectId = subjectId;
        this.role = role;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public void rotate(String newToken, LocalDateTime newExpiresAt) {
        this.token = newToken;
        this.expiresAt = newExpiresAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}
