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
 * 커뮤니티 게시글. 기본은 익명이고, author는 익명 글에서 API 응답에 절대 노출하지 않으며
 * anon_name(PostAnonParticipant)으로만 나간다 (설계 문서 4장).
 * 작성 화면의 실명 토글을 켠 글만 작성자 이름이 노출된다.
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

    @Column(nullable = false)
    private boolean anonymous;

    public Post(String id, University university, Student author, String title, String content, String originalLang,
                boolean anonymous) {
        this.id = id;
        this.university = university;
        this.author = author;
        this.title = title;
        this.content = content;
        this.originalLang = originalLang;
        this.anonymous = anonymous;
        this.likeCount = 0;
        this.commentCount = 0;
    }

    /** 수정 시 본문 언어가 바뀔 수 있어 originalLang도 같이 다시 받는다. */
    public void update(String title, String content, String originalLang) {
        this.title = title;
        this.content = content;
        this.originalLang = originalLang;
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
