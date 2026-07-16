package com.HelloMate.HelloMateBackend.domain.community.repository;

import com.HelloMate.HelloMateBackend.domain.community.entity.PostAnonParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostAnonParticipantRepository extends JpaRepository<PostAnonParticipant, String> {

    Optional<PostAnonParticipant> findByPostIdAndStudentId(String postId, String studentId);

    long countByPostId(String postId);
}
