package com.HelloMate.HelloMateBackend.domain.notice.dto.request;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** 발송 후 오탈자 수정용. 수신자 목록은 바뀌지 않는다. */
public record UpdateNoticeRequest(
        @Size(max = 255) String title,
        String content,
        NoticeType type,
        LocalDate bannerStartDate,
        LocalDate bannerEndDate
) {
}
