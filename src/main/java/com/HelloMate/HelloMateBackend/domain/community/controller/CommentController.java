package com.HelloMate.HelloMateBackend.domain.community.controller;

import com.HelloMate.HelloMateBackend.domain.community.dto.response.MyCommentResponse;
import com.HelloMate.HelloMateBackend.domain.community.entity.PostComment;
import com.HelloMate.HelloMateBackend.domain.community.service.PostService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import com.HelloMate.HelloMateBackend.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final PostService postService;

    @GetMapping("/mine")
    public ApiResponse<List<MyCommentResponse>> getMyComments(@CurrentUser AuthPrincipal principal,
                                                                @RequestParam(required = false) String cursor,
                                                                @RequestParam(defaultValue = "20") int limit) {
        Slice<PostComment> slice = postService.getMyCommentSlice(principal.id(), cursor, limit);
        return ApiResponse.ok(postService.toMyCommentList(slice), postService.commentCursorMetaOf(slice));
    }
}
