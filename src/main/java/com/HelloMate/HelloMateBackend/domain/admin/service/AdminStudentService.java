package com.HelloMate.HelloMateBackend.domain.admin.service;

import com.HelloMate.HelloMateBackend.domain.admin.dto.response.StudentDirectoryItemResponse;
import com.HelloMate.HelloMateBackend.domain.staff.service.StaffService;
import com.HelloMate.HelloMateBackend.domain.student.dto.response.StudentProfileResponse;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStudentService {

    private final StudentRepository studentRepository;
    private final StaffService staffService;
    private final StudentService studentService;

    public Page<StudentDirectoryItemResponse> getStudents(String staffId, String keyword, String country,
                                                           String major, String grade, int page, int size) {
        String universityId = staffService.getStaff(staffId).getUniversity().getId();
        Page<Student> students = studentRepository.search(
                universityId, keyword, country, major, grade, PageRequest.of(Math.max(page - 1, 0), size));
        return students.map(StudentDirectoryItemResponse::from);
    }

    public StudentProfileResponse getStudentDetail(String studentId) {
        return StudentProfileResponse.from(studentService.getStudent(studentId));
    }
}
