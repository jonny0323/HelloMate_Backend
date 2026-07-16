package com.HelloMate.HelloMateBackend.domain.admin.dto.response;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.entity.StudentType;

public record StudentDirectoryItemResponse(
        String id,
        String name,
        String email,
        String country,
        StudentType studentType,
        String major,
        String grade
) {
    public static StudentDirectoryItemResponse from(Student student) {
        return new StudentDirectoryItemResponse(student.getId(), student.getName(), student.getEmail(),
                student.getCountry(), student.getStudentType(), student.getMajor(), student.getGrade());
    }
}
