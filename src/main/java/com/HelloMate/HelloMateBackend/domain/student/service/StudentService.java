package com.HelloMate.HelloMateBackend.domain.student.service;

import com.HelloMate.HelloMateBackend.domain.auth.entity.EmailVerificationPurpose;
import com.HelloMate.HelloMateBackend.domain.auth.repository.RefreshTokenRepository;
import com.HelloMate.HelloMateBackend.domain.auth.service.EmailVerificationService;
import com.HelloMate.HelloMateBackend.domain.student.dto.request.StudentProfileUpdateRequest;
import com.HelloMate.HelloMateBackend.domain.student.dto.response.StudentProfileResponse;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.security.Role;
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
    private final RefreshTokenRepository refreshTokenRepository;
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
        refreshTokenRepository.deleteBySubjectIdAndRole(studentId, Role.STUDENT);
    }

    /**
     * 탈퇴는 상태 전환만 한다. post/post_comment가 users를 FK로 참조하고 있어 행을 지우면
     * 남아 있는 글/댓글이 통째로 깨진다. 개인정보 파기는 별도 배치의 몫으로 남긴다.
     */
    @Transactional
    public void withdraw(String studentId) {
        Student student = getStudent(studentId);
        if (student.isWithdrawn()) {
            throw new BusinessException(ErrorCode.ACCOUNT_WITHDRAWN);
        }
        student.withdraw();
        refreshTokenRepository.deleteBySubjectIdAndRole(studentId, Role.STUDENT);
    }

    public Student getStudent(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }
}
