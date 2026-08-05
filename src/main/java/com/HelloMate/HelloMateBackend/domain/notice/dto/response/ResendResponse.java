package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

/** 재발송은 미열람자에게만 나가므로 몇 명에게 갔는지 알려준다. */
public record ResendResponse(int notifiedCount) {
}
