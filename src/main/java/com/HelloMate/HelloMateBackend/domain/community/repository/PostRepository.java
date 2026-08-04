package com.HelloMate.HelloMateBackend.domain.community.repository;

import com.HelloMate.HelloMateBackend.domain.community.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<Post, String> {

    @Query("select p from Post p where p.university.id = :universityId "
            + "and (:keyword is null or p.title like %:keyword% or p.content like %:keyword%) "
            + "and (:cursor is null or p.createdAt < :cursor) order by p.createdAt desc")
    Slice<Post> findByUniversityIdOrderByCreatedAtDesc(@Param("universityId") String universityId,
                                                        @Param("keyword") String keyword,
                                                        @Param("cursor") LocalDateTime cursor,
                                                        Pageable pageable);

    @Query("select p from Post p where p.author.id = :authorId "
            + "and (:cursor is null or p.createdAt < :cursor) order by p.createdAt desc")
    Slice<Post> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") String authorId,
                                                    @Param("cursor") LocalDateTime cursor,
                                                    Pageable pageable);
}
