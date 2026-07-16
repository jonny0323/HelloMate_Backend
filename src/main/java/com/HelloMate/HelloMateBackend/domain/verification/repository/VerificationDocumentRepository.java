package com.HelloMate.HelloMateBackend.domain.verification.repository;

import com.HelloMate.HelloMateBackend.domain.verification.entity.VerificationDocument;
import com.HelloMate.HelloMateBackend.domain.verification.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, String> {

    Page<VerificationDocument> findByStatus(VerificationStatus status, Pageable pageable);
}
