package com.HelloMate.HelloMateBackend.domain.verification.dto.response;

import com.HelloMate.HelloMateBackend.domain.student.entity.StudentVerificationStatus;

/**
 * 학생 인증 화면이 "방식 선택 / 검토 중 / 완료 / 실패" 중 무엇을 그릴지 결정하는 응답.
 * 서류를 한 번도 안 낸 학생도 200으로 받아야 해서 latestDocument는 null 가능하다.
 */
public record VerificationStatusResponse(
        StudentVerificationStatus verificationStatus,
        boolean verified,
        VerificationDocumentResponse latestDocument
) {
}
