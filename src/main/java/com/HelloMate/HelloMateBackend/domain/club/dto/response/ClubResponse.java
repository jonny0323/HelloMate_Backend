package com.HelloMate.HelloMateBackend.domain.club.dto.response;

import com.HelloMate.HelloMateBackend.domain.club.entity.Club;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClubResponse(
        String id,
        String title,
        String introduction,
        String creatorId,
        String creatorName,
        int maxMembers,
        int currentMembers,
        boolean full,
        LocalDate deadline,
        LocalDateTime createdAt
) {
    public static ClubResponse from(Club club) {
        return new ClubResponse(
                club.getId(),
                club.getTitle(),
                club.getIntroduction(),
                club.getCreator().getId(),
                club.getCreator().getName(),
                club.getMaxMembers(),
                club.getCurrentMembers(),
                club.isFull(),
                club.getDeadline(),
                club.getCreatedAt()
        );
    }
}
