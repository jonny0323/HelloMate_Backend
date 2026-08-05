package com.HelloMate.HelloMateBackend.domain.student.dto.response;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.entity.StudentType;
import com.HelloMate.HelloMateBackend.domain.student.entity.StudentVerificationStatus;

public record StudentProfileResponse(
        String id,
        String loginId,
        String email,
        String name,
        String country,
        Integer birthYear,
        String language,
        StudentType studentType,
        String major,
        String grade,
        String universityName,
        StudentVerificationStatus verificationStatus,
        boolean verified
) {
    public static StudentProfileResponse from(Student student) {
        return new StudentProfileResponse(
                student.getId(),
                student.getLoginId(),
                student.getEmail(),
                student.getName(),
                student.getCountry(),
                student.getBirthYear(),
                student.getLanguage(),
                student.getStudentType(),
                student.getMajor(),
                student.getGrade(),
                student.getUniversity().getName(),
                student.getVerificationStatus(),
                student.isVerified()
        );
    }
}
