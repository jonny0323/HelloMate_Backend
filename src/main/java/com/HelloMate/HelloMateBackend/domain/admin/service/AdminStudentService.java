package com.HelloMate.HelloMateBackend.domain.admin.service;

import com.HelloMate.HelloMateBackend.domain.admin.dto.response.StudentDirectoryItemResponse;
import com.HelloMate.HelloMateBackend.domain.admin.dto.response.TargetGroupResponse;
import com.HelloMate.HelloMateBackend.domain.admin.dto.response.TargetGroupType;
import com.HelloMate.HelloMateBackend.domain.admin.util.CountryLabel;
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

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 공지 작성 화면 '학과 · 국가별' 탭 카드 목록.
     * 학생이 하나도 없는 국가/학과는 내려주지 않는다 — 고르면 수신자 0명으로 발송이 막히는 카드다.
     */
    public List<TargetGroupResponse> getTargetGroups(String staffId) {
        String universityId = staffService.getStaff(staffId).getUniversity().getId();

        List<TargetGroupResponse> groups = new ArrayList<>();
        studentRepository.countActiveGroupByCountry(universityId).stream()
                .map(g -> new TargetGroupResponse(g.groupKey(), CountryLabel.of(g.groupKey()),
                        TargetGroupType.COUNTRY, g.count()))
                .forEach(groups::add);
        studentRepository.countActiveGroupByMajor(universityId).stream()
                .map(g -> new TargetGroupResponse(g.groupKey(), g.groupKey(), TargetGroupType.MAJOR, g.count()))
                .forEach(groups::add);
        return groups;
    }

    public StudentProfileResponse getStudentDetail(String studentId) {
        return StudentProfileResponse.from(studentService.getStudent(studentId));
    }
}
