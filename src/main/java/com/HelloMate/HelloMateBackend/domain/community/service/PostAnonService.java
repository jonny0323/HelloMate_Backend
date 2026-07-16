package com.HelloMate.HelloMateBackend.domain.community.service;

import com.HelloMate.HelloMateBackend.domain.community.entity.Post;
import com.HelloMate.HelloMateBackend.domain.community.entity.PostAnonParticipant;
import com.HelloMate.HelloMateBackend.domain.community.repository.PostAnonParticipantRepository;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글(스레드) 내에서 학생별로 고정된 "익명 N" 번호를 배정/조회한다.
 * student_id는 이 서비스 밖으로 절대 노출하지 않는다 (설계 문서 4장).
 */
@Service
@RequiredArgsConstructor
public class PostAnonService {

    private final PostAnonParticipantRepository postAnonParticipantRepository;

    @Transactional
    public String getOrAssignAnonName(Post post, Student student) {
        return postAnonParticipantRepository.findByPostIdAndStudentId(post.getId(), student.getId())
                .map(p -> "익명 " + p.getAnonNumber())
                .orElseGet(() -> {
                    int nextNumber = (int) postAnonParticipantRepository.countByPostId(post.getId()) + 1;
                    postAnonParticipantRepository.save(
                            new PostAnonParticipant(UuidCreator.create(), post, student, nextNumber));
                    return "익명 " + nextNumber;
                });
    }
}
