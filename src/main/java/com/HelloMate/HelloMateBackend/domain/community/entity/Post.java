package com.HelloMate.HelloMateBackend.domain.community.entity;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.university.entity.University;
import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 전 화면 익명 커뮤니티 게시글. author는 API 응답에 절대 노출하지 않고
 * anon_name(PostAnonParticipant)으로만 노출한다 (설계 문서 4장).
 */
@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Student author;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 10)
    private String originalLang;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int commentCount;

    public Post(String id, University university, Student author, String title, String content, String originalLang) {
        this.id = id;
        this.university = university;
        this.author = author;
        this.title = title;
        this.content = content;
        this.originalLang = originalLang;
        this.likeCount = 0;
        this.commentCount = 0;
    }

    public void increaseLike() {
        this.likeCount++;
    }

    public void decreaseLike() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    public void increaseComment() {
        this.commentCount++;
    }

    public void decreaseComment() {
        this.commentCount = Math.max(0, this.commentCount - 1);
    }
}
