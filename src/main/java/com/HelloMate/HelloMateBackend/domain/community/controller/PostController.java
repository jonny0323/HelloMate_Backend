package com.HelloMate.HelloMateBackend.domain.community.controller;

import com.HelloMate.HelloMateBackend.domain.community.dto.request.CreateCommentRequest;
import com.HelloMate.HelloMateBackend.domain.community.dto.request.CreatePostRequest;
import com.HelloMate.HelloMateBackend.domain.community.dto.request.ReportRequest;
import com.HelloMate.HelloMateBackend.domain.community.dto.response.CreatePostResponse;
import com.HelloMate.HelloMateBackend.domain.community.dto.response.PostCommentResponse;
import com.HelloMate.HelloMateBackend.domain.community.dto.response.PostDetailResponse;
import com.HelloMate.HelloMateBackend.domain.community.dto.response.PostSummaryResponse;
import com.HelloMate.HelloMateBackend.domain.community.entity.Post;
import com.HelloMate.HelloMateBackend.domain.community.service.PostReportService;
import com.HelloMate.HelloMateBackend.domain.community.service.PostService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostReportService postReportService;

    @GetMapping
    public ApiResponse<List<PostSummaryResponse>> getPosts(@CurrentUser AuthPrincipal principal,
                                                            @RequestParam(required = false) String cursor,
                                                            @RequestParam(defaultValue = "20") int limit) {
        Slice<Post> slice = postService.getPostSlice(principal.id(), cursor, limit);
        return ApiResponse.ok(postService.toSummaryList(slice), postService.cursorMetaOf(slice));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreatePostResponse> createPost(@CurrentUser AuthPrincipal principal,
                                                       @Valid @RequestBody CreatePostRequest request) {
        return ApiResponse.ok(postService.createPost(principal.id(), request));
    }

    @GetMapping("/mine")
    public ApiResponse<List<PostSummaryResponse>> getMyPosts(@CurrentUser AuthPrincipal principal,
                                                              @RequestParam(required = false) String cursor,
                                                              @RequestParam(defaultValue = "20") int limit) {
        Slice<Post> slice = postService.getMyPostSlice(principal.id(), cursor, limit);
        return ApiResponse.ok(postService.toSummaryList(slice), postService.cursorMetaOf(slice));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponse> getPostDetail(@CurrentUser AuthPrincipal principal,
                                                          @PathVariable String postId,
                                                          @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
        return ApiResponse.ok(postService.getPostDetail(principal.id(), postId, acceptLanguage));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@CurrentUser AuthPrincipal principal, @PathVariable String postId) {
        postService.deletePost(principal, postId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<Void> like(@CurrentUser AuthPrincipal principal, @PathVariable String postId) {
        postService.like(principal.id(), postId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{postId}/like")
    public ApiResponse<Void> unlike(@CurrentUser AuthPrincipal principal, @PathVariable String postId) {
        postService.unlike(principal.id(), postId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<List<PostCommentResponse>> getComments(@CurrentUser AuthPrincipal principal,
                                                               @PathVariable String postId,
                                                               @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage) {
        return ApiResponse.ok(postService.getPostDetail(principal.id(), postId, acceptLanguage).comments());
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostCommentResponse> createComment(@CurrentUser AuthPrincipal principal,
                                                           @PathVariable String postId,
                                                           @Valid @RequestBody CreateCommentRequest request) {
        return ApiResponse.ok(postService.createComment(principal.id(), postId, request));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@CurrentUser AuthPrincipal principal, @PathVariable String postId,
                                            @PathVariable String commentId) {
        postService.deleteComment(principal, postId, commentId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{postId}/comments/{commentId}/like")
    public ApiResponse<Void> likeComment(@CurrentUser AuthPrincipal principal, @PathVariable String postId,
                                          @PathVariable String commentId) {
        postService.likeComment(principal.id(), postId, commentId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{postId}/comments/{commentId}/like")
    public ApiResponse<Void> unlikeComment(@CurrentUser AuthPrincipal principal, @PathVariable String postId,
                                            @PathVariable String commentId) {
        postService.unlikeComment(principal.id(), postId, commentId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{postId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> reportPost(@CurrentUser AuthPrincipal principal, @PathVariable String postId,
                                         @Valid @RequestBody ReportRequest request) {
        postReportService.reportPost(principal.id(), postId, request.reason());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{postId}/comments/{commentId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> reportComment(@CurrentUser AuthPrincipal principal, @PathVariable String postId,
                                            @PathVariable String commentId, @Valid @RequestBody ReportRequest request) {
        postReportService.reportComment(principal.id(), commentId, request.reason());
        return ApiResponse.ok(null);
    }
}
