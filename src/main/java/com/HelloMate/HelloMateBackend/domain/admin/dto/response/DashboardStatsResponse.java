package com.HelloMate.HelloMateBackend.domain.admin.dto.response;

/**
 * 대시보드 상단 통계 카드 4종.
 *
 * averageReadRate는 가중 평균(Σ열람 / Σ수신자)이다. 공지별 열람률을 단순 평균하면
 * 수신자 3명짜리 공지가 312명짜리와 같은 무게를 가져 수치가 왜곡된다.
 * pendingReplyCount는 스레드 기준 — 화면의 사이드바 뱃지와 같은 정의를 쓴다.
 */
public record DashboardStatsResponse(
        long sentNoticeCount,
        double averageReadRate,
        long activeStudentCount,
        long pendingReplyCount
) {
}
