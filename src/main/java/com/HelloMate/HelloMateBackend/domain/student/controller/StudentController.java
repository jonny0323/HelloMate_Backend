package com.HelloMate.HelloMateBackend.domain.student.controller;

import com.HelloMate.HelloMateBackend.domain.student.dto.request.StudentPasswordChangeRequest;
import com.HelloMate.HelloMateBackend.domain.student.dto.request.StudentProfileUpdateRequest;
import com.HelloMate.HelloMateBackend.domain.student.dto.response.StudentProfileResponse;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students/me")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ApiResponse<StudentProfileResponse> getMyProfile(@CurrentUser AuthPrincipal principal) {
        return ApiResponse.ok(studentService.getMyProfile(principal.id()));
    }

    @PatchMapping
    public ApiResponse<StudentProfileResponse> updateMyProfile(@CurrentUser AuthPrincipal principal,
                                                                @RequestBody StudentProfileUpdateRequest request) {
        return ApiResponse.ok(studentService.updateMyProfile(principal.id(), request));
    }

    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(@CurrentUser AuthPrincipal principal,
                                             @Valid @RequestBody StudentPasswordChangeRequest request) {
        studentService.changePassword(principal.id(), request.code(), request.newPassword());
        return ApiResponse.ok(null);
    }

    @DeleteMapping
    public ApiResponse<Void> withdraw(@CurrentUser AuthPrincipal principal) {
        studentService.withdraw(principal.id());
        return ApiResponse.ok(null);
    }
}
