package com.HelloMate.HelloMateBackend.domain.club.dto.response;

import com.HelloMate.HelloMateBackend.domain.club.entity.ClubMember;

import java.time.LocalDateTime;

public record ClubMemberResponse(String studentId, String studentName, LocalDateTime joinedAt) {
    public static ClubMemberResponse from(ClubMember member) {
        return new ClubMemberResponse(member.getStudent().getId(), member.getStudent().getName(), member.getCreatedAt());
    }
}
