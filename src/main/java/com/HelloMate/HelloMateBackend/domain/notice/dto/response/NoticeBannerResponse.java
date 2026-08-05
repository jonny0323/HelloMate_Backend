package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

import com.HelloMate.HelloMateBackend.domain.notice.entity.Notice;

import java.time.LocalDate;

/** 공지 홈 상단의 '중요' 배너 캐러셀 카드. */
public record NoticeBannerResponse(
        String id,
        String title,
        String department,
        LocalDate bannerStartDate,
        LocalDate bannerEndDate
) {
    public static NoticeBannerResponse from(Notice notice) {
        return new NoticeBannerResponse(notice.getId(), notice.getTitle(), notice.getDepartment(),
                notice.getBannerStartDate(), notice.getBannerEndDate());
    }
}
