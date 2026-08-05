package com.HelloMate.HelloMateBackend.domain.club.entity;

import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClubTest {

    private Club club(int maxMembers, LocalDate deadline) {
        return new Club("club-1", null, null, "저녁 축구 경기", "같이 뛰어요", maxMembers, deadline);
    }

    @Test
    @DisplayName("정원이 차면 카드 상태가 마감으로 바뀐다")
    void closedWhenFull() {
        Club club = club(2, LocalDate.now().plusDays(7));
        club.join();
        assertThat(club.resolveCardState(false)).isEqualTo(ClubCardState.JOINABLE);

        club.join();
        assertThat(club.isRecruitClosed()).isTrue();
        assertThat(club.remainingSeats()).isZero();
        assertThat(club.resolveCardState(false)).isEqualTo(ClubCardState.CLOSED);
    }

    @Test
    @DisplayName("정원을 넘겨 참여하면 CLUB_FULL로 거부된다")
    void rejectWhenOverCapacity() {
        Club club = club(1, LocalDate.now().plusDays(7));
        club.join();

        assertThatThrownBy(club::join)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CLUB_FULL);
    }

    @Test
    @DisplayName("자리가 남아도 마감일이 지났으면 참여할 수 없다")
    void rejectWhenDeadlinePassed() {
        Club club = club(10, LocalDate.now().minusDays(1));

        assertThat(club.isRecruitClosed()).isTrue();
        assertThatThrownBy(club::join)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CLUB_RECRUIT_CLOSED);
    }

    @Test
    @DisplayName("마감일 당일에는 아직 모집 중이다")
    void openOnDeadlineDay() {
        Club club = club(10, LocalDate.now());

        assertThat(club.isDeadlinePassed()).isFalse();
        assertThat(club.resolveCardState(false)).isEqualTo(ClubCardState.JOINABLE);
    }

    @Test
    @DisplayName("이미 참여한 클럽은 마감 여부와 무관하게 '참여 중'으로 표시된다")
    void joinedStateWinsOverClosed() {
        Club club = club(1, LocalDate.now().plusDays(7));
        club.join();

        assertThat(club.resolveCardState(true)).isEqualTo(ClubCardState.JOINED);
    }
}
