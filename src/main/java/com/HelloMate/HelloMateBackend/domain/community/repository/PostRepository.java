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
            + "and (:cursor is null or p.createdAt < :cursor) order by p.createdAt desc")
    Slice<Post> findByUniversityIdOrderByCreatedAtDesc(@Param("universityId") String universityId,
                                                        @Param("cursor") LocalDateTime cursor,
                                                        Pageable pageable);
}
