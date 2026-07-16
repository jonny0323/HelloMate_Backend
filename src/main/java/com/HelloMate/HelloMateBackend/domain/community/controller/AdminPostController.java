package com.HelloMate.HelloMateBackend.domain.community.controller;

import com.HelloMate.HelloMateBackend.domain.community.dto.response.FlaggedPostResponse;
import com.HelloMate.HelloMateBackend.domain.community.entity.PostReport;
import com.HelloMate.HelloMateBackend.domain.community.service.PostReportService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.common.response.PageMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 설계 문서 4장 갭 보완: 커뮤니티 모더레이션 (신고 큐 조회 + 강제 삭제).
 */
@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostReportService postReportService;

    @GetMapping
    public ApiResponse<List<FlaggedPostResponse>> getFlaggedPosts(@RequestParam(defaultValue = "false") boolean flagged,
                                                                    @RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "20") int size) {
        Page<PostReport> reports = postReportService.getFlaggedPosts(page, size);
        List<FlaggedPostResponse> content = reports.map(FlaggedPostResponse::from).getContent();
        return ApiResponse.ok(content, new PageMeta(page, size, reports.getTotalElements()));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable String postId) {
        postReportService.deletePostAsAdmin(postId);
        return ApiResponse.ok(null);
    }
}
