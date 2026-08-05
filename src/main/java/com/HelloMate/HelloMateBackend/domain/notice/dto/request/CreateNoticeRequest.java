package com.HelloMate.HelloMateBackend.domain.notice.dto.request;

import com.HelloMate.HelloMateBackend.domain.notice.entity.NoticeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * department는 받지 않는다 — 작성자(Staff)의 소속 부서를 서버가 굳힌다.
 * 요청으로 받으면 국제교류처 담당자가 '장학처' 이름으로 공지를 보낼 수 있다(부서 사칭).
 */
public record CreateNoticeRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String content,
        @NotNull NoticeType type,
        @NotNull @Valid AudienceRequest audience,
        List<String> files,
        LocalDate bannerStartDate,
        LocalDate bannerEndDate
) {
}
