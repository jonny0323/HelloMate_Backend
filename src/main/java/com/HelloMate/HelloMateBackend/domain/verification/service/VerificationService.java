package com.HelloMate.HelloMateBackend.domain.verification.service;

import com.HelloMate.HelloMateBackend.domain.file.entity.UploadedFile;
import com.HelloMate.HelloMateBackend.domain.file.repository.UploadedFileRepository;
import com.HelloMate.HelloMateBackend.domain.notification.entity.NotificationCategory;
import com.HelloMate.HelloMateBackend.domain.notification.service.NotificationService;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.domain.verification.dto.request.ReviewVerificationDocumentRequest;
import com.HelloMate.HelloMateBackend.domain.verification.dto.request.SubmitVerificationDocumentRequest;
import com.HelloMate.HelloMateBackend.domain.verification.dto.response.VerificationDocumentResponse;
import com.HelloMate.HelloMateBackend.domain.verification.dto.response.VerificationStatusResponse;
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
    private final NotificationService notificationService;

    @Transactional
    public VerificationDocumentResponse submit(String studentId, SubmitVerificationDocumentRequest request) {
        Student student = studentService.getStudent(studentId);
        UploadedFile file = uploadedFileRepository.findById(request.fileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        VerificationDocument document = new VerificationDocument(
                UuidCreator.create(), student, file, request.documentType());
        verificationDocumentRepository.save(document);
        student.submitVerificationDocument();
        return VerificationDocumentResponse.from(document);
    }

    /**
     * 서류를 한 번도 안 낸 학생(이메일 인증만 한 학생 포함)도 화면 분기를 해야 하므로
     * 404 대신 latestDocument=null로 200을 준다.
     */
    public VerificationStatusResponse getMyStatus(String studentId) {
        Student student = studentService.getStudent(studentId);
        VerificationDocumentResponse latest = verificationDocumentRepository
                .findTopByStudentIdOrderByCreatedAtDesc(studentId)
                .map(VerificationDocumentResponse::from)
                .orElse(null);
        return new VerificationStatusResponse(student.getVerificationStatus(), student.isVerified(), latest);
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
        Student student = document.getStudent();

        if (request.status() == VerificationStatus.APPROVED) {
            document.approve(reviewer);
            student.approveVerificationDocument();
            notificationService.notify(student, NotificationCategory.SYSTEM,
                    "학생 인증이 완료되었어요", "verification", document.getId());
        } else {
            if (request.rejectReason() == null || request.rejectReason().isBlank()) {
                throw new BusinessException(ErrorCode.REJECT_REASON_REQUIRED);
            }
            document.reject(reviewer, request.rejectReason());
            student.rejectVerificationDocument();
            notificationService.notify(student, NotificationCategory.SYSTEM,
                    "학생 인증 서류를 다시 확인해 주세요", "verification", document.getId());
        }
        return VerificationDocumentResponse.from(document);
    }
}
