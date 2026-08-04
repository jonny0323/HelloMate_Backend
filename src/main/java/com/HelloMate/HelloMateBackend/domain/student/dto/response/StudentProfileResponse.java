package com.HelloMate.HelloMateBackend.domain.student.dto.response;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.entity.StudentType;

public record StudentProfileResponse(
        String id,
        String email,
        String name,
        String country,
        Integer birthYear,
        String language,
        StudentType studentType,
        String major,
        String grade,
        String universityName
) {
    public static StudentProfileResponse from(Student student) {
        return new StudentProfileResponse(
                student.getId(),
                student.getEmail(),
                student.getName(),
                student.getCountry(),
                student.getBirthYear(),
                student.getLanguage(),
                student.getStudentType(),
                student.getMajor(),
                student.getGrade(),
                student.getUniversity().getName()
        );
    }
}
