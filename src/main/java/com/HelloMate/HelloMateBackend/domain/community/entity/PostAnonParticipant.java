package com.HelloMate.HelloMateBackend.domain.community.entity;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 스레드 내에서 학생별로 고정 배정되는 "익명 N" 번호.
 * 다른 참여자에게는 이 번호만 노출되고 student_id는 절대 응답에 포함하지 않는다 (설계 문서 4장).
 */
@Entity
@Table(name = "post_anon_participant", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "student_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostAnonParticipant {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private int anonNumber;

    public PostAnonParticipant(String id, Post post, Student student, int anonNumber) {
        this.id = id;
        this.post = post;
        this.student = student;
        this.anonNumber = anonNumber;
    }
}
