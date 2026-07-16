package com.HelloMate.HelloMateBackend.domain.translation.service;

import com.HelloMate.HelloMateBackend.domain.translation.dto.response.OfficialTranslateResponse;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslateResponse;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslatedContent;
import com.HelloMate.HelloMateBackend.domain.translation.entity.TranslationContentType;

import java.util.Optional;

/**
 * 설계 문서 10장의 번역 아키텍처(NLLB-200, 경량 모델/대형 모델 분리)를 반영한 인터페이스.
 * 실제 ML 서버 연동 전까지는 {@link StubTranslationService}로 동작한다.
 */
public interface TranslationService {

    TranslateResponse translate(String text, String targetLang);

    OfficialTranslateResponse translateOfficial(String text, String targetLang);

    /**
     * 게시글/공지 등 콘텐츠 조회 시 캐시를 우선 사용해 번역 결과를 반환한다 (캐시 미스 시 즉시 번역 후 캐시에 저장).
     */
    Optional<TranslatedContent> getOrTranslate(TranslationContentType contentType, String contentId,
                                                String originalText, String originalLang, String targetLang);
}
