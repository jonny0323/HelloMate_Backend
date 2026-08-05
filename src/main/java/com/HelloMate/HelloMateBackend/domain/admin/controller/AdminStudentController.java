package com.HelloMate.HelloMateBackend.domain.admin.controller;

import com.HelloMate.HelloMateBackend.domain.admin.dto.response.StudentDirectoryItemResponse;
import com.HelloMate.HelloMateBackend.domain.admin.dto.response.TargetGroupResponse;
import com.HelloMate.HelloMateBackend.domain.admin.service.AdminStudentService;
import com.HelloMate.HelloMateBackend.domain.student.dto.response.StudentProfileResponse;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.common.response.PageMeta;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/students")
@RequiredArgsConstructor
public class AdminStudentController {

    private final AdminStudentService adminStudentService;

    @GetMapping
    public ApiResponse<List<StudentDirectoryItemResponse>> getStudents(@CurrentUser AuthPrincipal principal,
                                                                        @RequestParam(required = false) String keyword,
                                                                        @RequestParam(required = false) String country,
                                                                        @RequestParam(required = false) String major,
                                                                        @RequestParam(required = false) String grade,
                                                                        @RequestParam(defaultValue = "1") int page,
                                                                        @RequestParam(defaultValue = "20") int size) {
        Page<StudentDirectoryItemResponse> result = adminStudentService.getStudents(principal.id(), keyword, country,
                major, grade, page, size);
        return ApiResponse.ok(result.getContent(), new PageMeta(page, size, result.getTotalElements()));
    }

    /** 공지 작성 화면의 '학과 · 국가별' 탭. {studentId} 매핑보다 먼저 잡히도록 리터럴 경로로 둔다. */
    @GetMapping("/target-groups")
    public ApiResponse<List<TargetGroupResponse>> getTargetGroups(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(adminStudentService.getTargetGroups(principal.id()));
    }

    @GetMapping("/{studentId}")
    public ApiResponse<StudentProfileResponse> getStudentDetail(@PathVariable String studentId) {
        return ApiResponse.ok(adminStudentService.getStudentDetail(studentId));
    }
}
