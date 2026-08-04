package com.HelloMate.HelloMateBackend.domain.student.service;

import com.HelloMate.HelloMateBackend.domain.auth.entity.EmailVerificationPurpose;
import com.HelloMate.HelloMateBackend.domain.auth.service.EmailVerificationService;
import com.HelloMate.HelloMateBackend.domain.student.dto.request.StudentProfileUpdateRequest;
import com.HelloMate.HelloMateBackend.domain.student.dto.response.StudentProfileResponse;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    public StudentProfileResponse getMyProfile(String studentId) {
        return StudentProfileResponse.from(getStudent(studentId));
    }

    @Transactional
    public StudentProfileResponse updateMyProfile(String studentId, StudentProfileUpdateRequest request) {
        Student student = getStudent(studentId);
        student.updateProfile(request.name(), request.country(), request.birthYear(),
                request.language(), request.major(), request.grade());
        return StudentProfileResponse.from(student);
    }

    @Transactional
    public void changePassword(String studentId, String code, String newPassword) {
        Student student = getStudent(studentId);
        emailVerificationService.confirmCode(student.getEmail(), code, EmailVerificationPurpose.PASSWORD_RESET);
        student.updatePassword(passwordEncoder.encode(newPassword));
    }

    public Student getStudent(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }
}
