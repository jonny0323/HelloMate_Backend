package com.HelloMate.HelloMateBackend.domain.community.service;

import com.HelloMate.HelloMateBackend.domain.community.dto.request.CreateCommentRequest;
import com.HelloMate.HelloMateBackend.domain.community.dto.request.CreatePostRequest;
import com.HelloMate.HelloMateBackend.domain.community.dto.response.CreatePostResponse;
import com.HelloMate.HelloMateBackend.domain.community.dto.response.MyCommentResponse;
import com.HelloMate.HelloMateBackend.domain.community.dto.response.PostCommentResponse;
import com.HelloMate.HelloMateBackend.domain.community.dto.response.PostDetailResponse;
import com.HelloMate.HelloMateBackend.domain.community.dto.response.PostSummaryResponse;
import com.HelloMate.HelloMateBackend.domain.community.entity.Post;
import com.HelloMate.HelloMateBackend.domain.community.entity.PostComment;
import com.HelloMate.HelloMateBackend.domain.community.entity.PostCommentLike;
import com.HelloMate.HelloMateBackend.domain.community.entity.PostLike;
import com.HelloMate.HelloMateBackend.domain.community.repository.PostCommentLikeRepository;
import com.HelloMate.HelloMateBackend.domain.community.repository.PostCommentRepository;
import com.HelloMate.HelloMateBackend.domain.community.repository.PostLikeRepository;
import com.HelloMate.HelloMateBackend.domain.community.repository.PostRepository;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.service.StudentService;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslatedContent;
import com.HelloMate.HelloMateBackend.domain.translation.entity.TranslationContentType;
import com.HelloMate.HelloMateBackend.domain.translation.service.LanguageDetector;
import com.HelloMate.HelloMateBackend.domain.translation.service.TranslationService;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import com.HelloMate.HelloMateBackend.global.common.response.CursorMeta;
import com.HelloMate.HelloMateBackend.global.common.util.AcceptLanguageUtil;
import com.HelloMate.HelloMateBackend.global.common.util.CursorPageUtil;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import com.HelloMate.HelloMateBackend.global.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentLikeRepository postCommentLikeRepository;
    private final PostAnonService postAnonService;
    private final StudentService studentService;
    private final TranslationService translationService;

    @Transactional
    public CreatePostResponse createPost(String studentId, CreatePostRequest request) {
        Student author = studentService.getStudent(studentId);
        String originalLang = LanguageDetector.detect(request.content());
        Post post = new Post(UuidCreator.create(), author.getUniversity(), author, request.title(),
                request.content(), originalLang);
        postRepository.save(post);
        String anonName = postAnonService.getOrAssignAnonName(post, author);
        return new CreatePostResponse(post.getId(), anonName, post.getCreatedAt());
    }

    public Slice<Post> getPostSlice(String studentId, String cursor, int limit) {
        Student student = studentService.getStudent(studentId);
        return postRepository.findByUniversityIdOrderByCreatedAtDesc(student.getUniversity().getId(),
                CursorPageUtil.decode(cursor), PageRequest.of(0, limit));
    }

    @Transactional
    public List<PostSummaryResponse> toSummaryList(Slice<Post> slice) {
        return slice.getContent().stream()
                .map(post -> PostSummaryResponse.of(post, postAnonService.getOrAssignAnonName(post, post.getAuthor())))
                .toList();
    }

    public CursorMeta cursorMetaOf(Slice<Post> slice) {
        String nextCursor = slice.hasNext() && !slice.getContent().isEmpty()
                ? CursorPageUtil.encode(slice.getContent().get(slice.getContent().size() - 1).getCreatedAt())
                : null;
        return new CursorMeta(nextCursor, slice.hasNext(), slice.getContent().size());
    }

    public Slice<Post> getMyPostSlice(String studentId, String cursor, int limit) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(studentId, CursorPageUtil.decode(cursor), PageRequest.of(0, limit));
    }

    public Slice<PostComment> getMyCommentSlice(String studentId, String cursor, int limit) {
        return postCommentRepository.findByAuthorIdOrderByCreatedAtDesc(studentId, CursorPageUtil.decode(cursor), PageRequest.of(0, limit));
    }

    @Transactional
    public List<MyCommentResponse> toMyCommentList(Slice<PostComment> slice) {
        return slice.getContent().stream()
                .map(comment -> new MyCommentResponse(
                        comment.getId(),
                        comment.getPost().getId(),
                        comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                        postAnonService.getOrAssignAnonName(comment.getPost(), comment.getAuthor()),
                        comment.getContent(),
                        comment.getOriginalLang(),
                        comment.getLikeCount(),
                        comment.getCreatedAt()))
                .toList();
    }

    public CursorMeta commentCursorMetaOf(Slice<PostComment> slice) {
        String nextCursor = slice.hasNext() && !slice.getContent().isEmpty()
                ? CursorPageUtil.encode(slice.getContent().get(slice.getContent().size() - 1).getCreatedAt())
                : null;
        return new CursorMeta(nextCursor, slice.hasNext(), slice.getContent().size());
    }

    @Transactional
    public PostDetailResponse getPostDetail(String studentId, String postId, String acceptLanguageHeader) {
        Post post = getPost(postId);
        Student viewer = studentService.getStudent(studentId);
        String targetLang = AcceptLanguageUtil.primaryLanguage(acceptLanguageHeader);

        String anonName = postAnonService.getOrAssignAnonName(post, post.getAuthor());
        boolean likedByMe = postLikeRepository.existsByPostIdAndStudentId(postId, studentId);
        TranslatedContent translated = resolveTranslation(TranslationContentType.POST, postId, post.getContent(),
                post.getOriginalLang(), targetLang);

        List<PostComment> comments = postCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        List<String> commentIds = comments.stream().map(PostComment::getId).toList();
        Set<String> likedCommentIds = commentIds.isEmpty() ? Set.of()
                : postCommentLikeRepository.findLikedCommentIds(studentId, commentIds);

        List<PostCommentResponse> commentResponses = comments.stream()
                .map(comment -> {
                    String commentAnonName = postAnonService.getOrAssignAnonName(post, comment.getAuthor());
                    TranslatedContent commentTranslated = resolveTranslation(TranslationContentType.POST_COMMENT,
                            comment.getId(), comment.getContent(), comment.getOriginalLang(), targetLang);
                    return new PostCommentResponse(
                            comment.getId(),
                            comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                            commentAnonName,
                            comment.getContent(),
                            comment.getOriginalLang(),
                            commentTranslated,
                            comment.getLikeCount(),
                            likedCommentIds.contains(comment.getId()),
                            comment.getCreatedAt()
                    );
                })
                .toList();

        return new PostDetailResponse(post.getId(), anonName, post.getTitle(), post.getOriginalLang(), post.getContent(),
                translated, post.getLikeCount(), likedByMe, post.getCommentCount(), commentResponses);
    }

    @Transactional
    public void deletePost(AuthPrincipal principal, String postId) {
        Post post = getPost(postId);
        if (principal.isStudent() && !post.getAuthor().getId().equals(principal.id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        postRepository.delete(post);
    }

    @Transactional
    public void like(String studentId, String postId) {
        Post post = getPost(postId);
        if (postLikeRepository.existsByPostIdAndStudentId(postId, studentId)) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED);
        }
        Student student = studentService.getStudent(studentId);
        postLikeRepository.save(new PostLike(UuidCreator.create(), post, student));
        post.increaseLike();
    }

    @Transactional
    public void unlike(String studentId, String postId) {
        getPost(postId);
        PostLike like = postLikeRepository.findByPostIdAndStudentId(postId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LIKED_YET));
        postLikeRepository.delete(like);
        like.getPost().decreaseLike();
    }

    @Transactional
    public PostCommentResponse createComment(String studentId, String postId, CreateCommentRequest request) {
        Post post = getPost(postId);
        Student author = studentService.getStudent(studentId);
        PostComment parent = request.parentCommentId() == null ? null
                : postCommentRepository.findById(request.parentCommentId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        String originalLang = LanguageDetector.detect(request.content());
        PostComment comment = new PostComment(UuidCreator.create(), post, author, parent, request.content(), originalLang);
        postCommentRepository.save(comment);
        post.increaseComment();

        String anonName = postAnonService.getOrAssignAnonName(post, author);
        return new PostCommentResponse(comment.getId(), parent == null ? null : parent.getId(), anonName,
                comment.getContent(), comment.getOriginalLang(), null, 0, false, comment.getCreatedAt());
    }

    @Transactional
    public void deleteComment(AuthPrincipal principal, String postId, String commentId) {
        PostComment comment = getComment(postId, commentId);
        if (principal.isStudent() && !comment.getAuthor().getId().equals(principal.id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        comment.getPost().decreaseComment();
        postCommentRepository.delete(comment);
    }

    @Transactional
    public void likeComment(String studentId, String postId, String commentId) {
        PostComment comment = getComment(postId, commentId);
        if (postCommentLikeRepository.findByCommentIdAndStudentId(commentId, studentId).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED);
        }
        Student student = studentService.getStudent(studentId);
        postCommentLikeRepository.save(new PostCommentLike(UuidCreator.create(), comment, student));
        comment.increaseLike();
    }

    @Transactional
    public void unlikeComment(String studentId, String postId, String commentId) {
        getComment(postId, commentId);
        PostCommentLike like = postCommentLikeRepository.findByCommentIdAndStudentId(commentId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LIKED_YET));
        postCommentLikeRepository.delete(like);
        like.getComment().decreaseLike();
    }

    public Post getPost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private PostComment getComment(String postId, String commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getPost().getId().equals(postId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        return comment;
    }

    private TranslatedContent resolveTranslation(TranslationContentType contentType, String contentId,
                                                  String originalText, String originalLang, String targetLang) {
        if (targetLang == null) {
            return null;
        }
        return translationService.getOrTranslate(contentType, contentId, originalText, originalLang, targetLang)
                .orElse(null);
    }
}
