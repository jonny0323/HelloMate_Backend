package com.HelloMate.HelloMateBackend.domain.admin.dto.response;

/**
 * 공지 작성 화면의 '학과 · 국가별' 탭 카드 한 장.
 *
 * groupKey는 그대로 {@code AudienceRequest.countryCodes / majors}에 실려 돌아온다 —
 * 화면이 라벨을 되돌려 보내면 국가명/코드 표기가 갈려서 수신자가 0명이 된다.
 */
public record TargetGroupResponse(String groupKey, String label, TargetGroupType type, long count) {
}
