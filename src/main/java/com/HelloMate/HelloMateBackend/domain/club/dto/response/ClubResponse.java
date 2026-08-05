package com.HelloMate.HelloMateBackend.domain.club.dto.response;

import com.HelloMate.HelloMateBackend.domain.club.entity.Club;
import com.HelloMate.HelloMateBackend.domain.club.entity.ClubCardState;

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
        int remainingSeats,
        boolean full,
        ClubCardState cardState,
        LocalDate deadline,
        LocalDateTime createdAt
) {
    public static ClubResponse of(Club club, boolean joined) {
        return new ClubResponse(
                club.getId(),
                club.getTitle(),
                club.getIntroduction(),
                club.getCreator().getId(),
                club.getCreator().getName(),
                club.getMaxMembers(),
                club.getCurrentMembers(),
                club.remainingSeats(),
                club.isFull(),
                club.resolveCardState(joined),
                club.getDeadline(),
                club.getCreatedAt()
        );
    }
}
