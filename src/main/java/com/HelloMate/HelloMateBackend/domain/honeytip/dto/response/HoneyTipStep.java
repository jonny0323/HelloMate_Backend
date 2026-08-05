package com.HelloMate.HelloMateBackend.domain.honeytip.dto.response;

import java.util.List;

/** 정보글 상세의 번호 STEP 카드 (원형 번호 + 제목 + 설명 + 해시태그). */
public record HoneyTipStep(int order, String title, String description, List<String> tags) {
}
