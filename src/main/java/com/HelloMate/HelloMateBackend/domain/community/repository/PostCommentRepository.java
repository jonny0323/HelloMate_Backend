package com.HelloMate.HelloMateBackend.domain.community.repository;

import com.HelloMate.HelloMateBackend.domain.community.entity.PostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, String> {

    List<PostComment> findByPostIdOrderByCreatedAtAsc(String postId);

    @Query("select c from PostComment c where c.author.id = :authorId "
            + "and (:cursor is null or c.createdAt < :cursor) order by c.createdAt desc")
    Slice<PostComment> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") String authorId,
                                                           @Param("cursor") LocalDateTime cursor,
                                                           Pageable pageable);
}
