package com.HelloMate.HelloMateBackend.domain.student.service;

import com.HelloMate.HelloMateBackend.domain.student.dto.request.StudentProfileUpdateRequest;
import com.HelloMate.HelloMateBackend.domain.student.dto.response.StudentProfileResponse;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentProfileResponse getMyProfile(String studentId) {
        return StudentProfileResponse.from(getStudent(studentId));
    }

    @Transactional
    public StudentProfileResponse updateMyProfile(String studentId, StudentProfileUpdateRequest request) {
        Student student = getStudent(studentId);
        student.updateProfile(request.language(), request.major(), request.grade());
        return StudentProfileResponse.from(student);
    }

    public Student getStudent(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }
}
