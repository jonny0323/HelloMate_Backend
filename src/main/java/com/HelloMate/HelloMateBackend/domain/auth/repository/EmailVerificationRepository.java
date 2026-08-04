package com.HelloMate.HelloMateBackend.domain.auth.repository;

import com.HelloMate.HelloMateBackend.domain.auth.entity.EmailVerification;
import com.HelloMate.HelloMateBackend.domain.auth.entity.EmailVerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {

    Optional<EmailVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, EmailVerificationPurpose purpose);

    Optional<EmailVerification> findByResetTokenAndPurpose(String resetToken, EmailVerificationPurpose purpose);
}
