package com.HelloMate.HelloMateBackend.domain.translation.dto.response;

/**
 * GET /posts/:id 등에서 원문과 함께 내려주는 번역 결과 (설계 문서 4장 응답 예시의 "translated" 필드).
 */
public record TranslatedContent(String lang, String text) {
}
