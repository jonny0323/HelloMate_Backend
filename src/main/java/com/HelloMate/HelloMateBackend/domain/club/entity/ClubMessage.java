package com.HelloMate.HelloMateBackend.domain.club.entity;

import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
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
 * 클럽 그룹 채팅. chat 도메인의 ChatThread는 (student_id, staff_id) 1:1 전용이라 여러 멤버가 있는
 * 방을 못 담아서, club_id를 방 키로 쓰는 별도 엔티티로 둔다 (설계: docs/roadmaps/club-chat.md).
 */
@Entity
@Table(name = "club_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubMessage extends BaseTimeEntity {

    @Id
    @Column(length = 255)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Student sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public ClubMessage(String id, Club club, Student sender, String content) {
        this.id = id;
        this.club = club;
        this.sender = sender;
        this.content = content;
    }
}
