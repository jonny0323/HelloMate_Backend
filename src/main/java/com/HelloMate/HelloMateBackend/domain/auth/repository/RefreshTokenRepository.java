package com.HelloMate.HelloMateBackend.domain.auth.repository;

import com.HelloMate.HelloMateBackend.domain.auth.entity.RefreshToken;
import com.HelloMate.HelloMateBackend.global.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteBySubjectIdAndRole(String subjectId, Role role);
}
