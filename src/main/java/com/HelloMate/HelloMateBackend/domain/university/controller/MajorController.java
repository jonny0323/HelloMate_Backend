package com.HelloMate.HelloMateBackend.domain.university.controller;

import com.HelloMate.HelloMateBackend.domain.university.dto.response.MajorResponse;
import com.HelloMate.HelloMateBackend.domain.university.service.MajorService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/universities")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    @GetMapping("/{universityId}/majors")
    public ApiResponse<List<MajorResponse>> searchMajors(@PathVariable String universityId,
                                                           @RequestParam(required = false) String query) {
        return ApiResponse.ok(majorService.search(universityId, query));
    }
}
