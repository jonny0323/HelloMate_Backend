package com.HelloMate.HelloMateBackend.domain.verification.service;

import com.HelloMate.HelloMateBackend.domain.file.entity.UploadedFile;
import com.HelloMate.HelloMateBackend.domain.file.repository.UploadedFileRepository;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.domain.verification.dto.request.ReviewVerificationDocumentRequest;
import com.HelloMate.HelloMateBackend.domain.verification.dto.response.VerificationDocumentResponse;
import com.HelloMate.HelloMateBackend.domain.verification.entity.VerificationDocument;
import com.HelloMate.HelloMateBackend.domain.verification.entity.VerificationStatus;
import com.HelloMate.HelloMateBackend.domain.verification.repository.VerificationDocumentRepository;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationService {

    private final VerificationDocumentRepository verificationDocumentRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final StudentService studentService;
    private final StaffService staffService;

    @Transactional
    public VerificationDocumentResponse submit(String studentId, String fileId) {
        Student student = studentService.getStudent(studentId);
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));
        VerificationDocument document = new VerificationDocument(UuidCreator.create(), student, file);
        verificationDocumentRepository.save(document);
        return VerificationDocumentResponse.from(document);
    }

    public VerificationDocumentResponse getMyDocument(String studentId) {
        VerificationDocument document = verificationDocumentRepository.findTopByStudentIdOrderByCreatedAtDesc(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_DOCUMENT_NOT_FOUND));
        return VerificationDocumentResponse.from(document);
    }

    public Page<VerificationDocumentResponse> getDocuments(VerificationStatus status, int page, int size) {
        Page<VerificationDocument> documents = status == null
                ? verificationDocumentRepository.findAll(PageRequest.of(Math.max(page - 1, 0), size))
                : verificationDocumentRepository.findByStatus(status, PageRequest.of(Math.max(page - 1, 0), size));
        return documents.map(VerificationDocumentResponse::from);
    }

    @Transactional
    public VerificationDocumentResponse review(String staffId, String documentId, ReviewVerificationDocumentRequest request) {
        Staff reviewer = staffService.getStaff(staffId);
        VerificationDocument document = verificationDocumentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_DOCUMENT_NOT_FOUND));
        document.review(request.status(), reviewer);
        return VerificationDocumentResponse.from(document);
    }
}
