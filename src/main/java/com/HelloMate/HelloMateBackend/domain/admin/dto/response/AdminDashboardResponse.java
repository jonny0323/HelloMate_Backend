package com.HelloMate.HelloMateBackend.domain.admin.dto.response;

import java.util.List;

public record AdminDashboardResponse(
        DashboardStatsResponse stats,
        List<DashboardNoticeItem> recentNotices,
        List<UnansweredThreadItem> unansweredThreads
) {
}
