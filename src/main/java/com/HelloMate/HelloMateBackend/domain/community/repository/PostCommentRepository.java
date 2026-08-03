package com.HelloMate.HelloMateBackend.domain.community.repository;

import com.HelloMate.HelloMateBackend.domain.community.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, String> {

    List<PostComment> findByPostIdOrderByCreatedAtAsc(String postId);
}
