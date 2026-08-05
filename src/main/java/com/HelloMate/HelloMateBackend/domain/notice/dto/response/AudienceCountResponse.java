package com.HelloMate.HelloMateBackend.domain.notice.dto.response;

/**
 * 발송 전 "실제로 몇 명에게 가는지"를 서버가 알려준다.
 * 화면이 그룹별 인원을 단순 합산하면 국가·학과가 겹치는 학생이 이중 계산된다.
 */
public record AudienceCountResponse(int recipientCount, String audienceLabel) {
}
