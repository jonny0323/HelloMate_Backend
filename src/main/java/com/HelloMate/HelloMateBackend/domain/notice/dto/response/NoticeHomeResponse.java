package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import java.util.List;

/**
 * 공지사항 홈 화면 한 벌. 배너와 최근 공지를 각각 요청하면 왕복이 두 번이라 하나로 묶었다.
 */
public record NoticeHomeResponse(
        List<NoticeBannerResponse> banners,
        List<StudentNoticeSummaryResponse> recentNotices
) {
}
