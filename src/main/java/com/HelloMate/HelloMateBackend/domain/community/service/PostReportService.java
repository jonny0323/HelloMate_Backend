package com.HelloMate.HelloMateBackend.domain.community.service;

import com.HelloMate.HelloMateBackend.domain.community.entity.Post;
import com.HelloMate.HelloMateBackend.domain.community.entity.PostComment;
import com.HelloMate.HelloMateBackend.domain.community.entity.PostReport;
import com.HelloMate.HelloMateBackend.domain.community.repository.PostCommentRepository;
import com.HelloMate.HelloMateBackend.domain.community.repository.PostRepository;
import com.HelloMate.HelloMateBackend.domain.community.repository.PostReportRepository;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 설계 문서 4장 갭 보완: 커뮤니티 신고/모더레이션.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReportService {

    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final StudentService studentService;

    @Transactional
    public void reportPost(String reporterId, String postId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        postReportRepository.save(new PostReport(UuidCreator.create(), post, null,
                studentService.getStudent(reporterId), reason));
    }

    @Transactional
    public void reportComment(String reporterId, String commentId, String reason) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        postReportRepository.save(new PostReport(UuidCreator.create(), null, comment,
                studentService.getStudent(reporterId), reason));
    }

    @Transactional
    public void deletePostAsAdmin(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        postRepository.delete(post);
    }

    public Page<PostReport> getFlaggedPosts(int page, int size) {
        return postReportRepository.findByPostIdIsNotNull(PageRequest.of(Math.max(page - 1, 0), size));
    }
}
