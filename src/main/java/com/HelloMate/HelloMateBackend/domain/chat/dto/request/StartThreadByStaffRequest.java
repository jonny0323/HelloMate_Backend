package com.HelloMate.HelloMateBackend.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 담당자가 학생에게 먼저 말을 거는 경우. 학생 앱은 공지 상세에서 학생이 먼저 여는 흐름만 있었다. */
public record StartThreadByStaffRequest(@NotBlank String studentId,
                                         @NotBlank @Size(max = 2000) String message) {
}
