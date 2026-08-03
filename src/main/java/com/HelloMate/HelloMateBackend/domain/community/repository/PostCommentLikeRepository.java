package com.HelloMate.HelloMateBackend.domain.community.repository;

import com.HelloMate.HelloMateBackend.domain.community.entity.PostCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostCommentLikeRepository extends JpaRepository<PostCommentLike, String> {

    Optional<PostCommentLike> findByCommentIdAndStudentId(String commentId, String studentId);

    @Query("select l.comment.id from PostCommentLike l where l.student.id = :studentId and l.comment.id in :commentIds")
    Set<String> findLikedCommentIds(@Param("studentId") String studentId, @Param("commentIds") List<String> commentIds);
}
