package com.HelloMate.HelloMateBackend.domain.community.repository;

import com.HelloMate.HelloMateBackend.domain.community.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, String> {

    Optional<PostLike> findByPostIdAndStudentId(String postId, String studentId);

    boolean existsByPostIdAndStudentId(String postId, String studentId);
}
