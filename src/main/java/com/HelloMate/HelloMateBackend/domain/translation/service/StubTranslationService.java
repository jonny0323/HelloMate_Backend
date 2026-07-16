package com.HelloMate.HelloMateBackend.domain.translation.service;

import com.HelloMate.HelloMateBackend.domain.translation.dto.response.OfficialTranslateResponse;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslateResponse;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslatedContent;
import com.HelloMate.HelloMateBackend.domain.translation.entity.TranslationCache;
import com.HelloMate.HelloMateBackend.domain.translation.entity.TranslationContentType;
import com.HelloMate.HelloMateBackend.domain.translation.repository.TranslationCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * 실제 NLLB-200 서버 연동 전까지 사용하는 스텁 구현체. 원문에 [target_lang] 접두어를 붙여
 * 반환하며, 몽골어 등 저자원 언어는 검수 필요 플래그를 true로 내린다 (설계 문서 10장).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StubTranslationService implements TranslationService {

    private static final Set<String> LOW_RESOURCE_LANGS = Set.of("mn");

    private final TranslationCacheRepository translationCacheRepository;

    @Override
    public TranslateResponse translate(String text, String targetLang) {
        String detectedSourceLang = LanguageDetector.detect(text);
        String translatedText = stubTranslate(text, targetLang);
        return new TranslateResponse(translatedText, detectedSourceLang, "stub-nllb-200-distilled");
    }

    @Override
    public OfficialTranslateResponse translateOfficial(String text, String targetLang) {
        String translatedText = stubTranslate(text, targetLang);
        boolean needsHumanReview = LOW_RESOURCE_LANGS.contains(targetLang);
        double confidence = needsHumanReview ? 0.71 : 0.92;
        return new OfficialTranslateResponse(translatedText, "stub-nllb-200-1.3B", needsHumanReview, confidence);
    }

    @Override
    @Transactional
    public Optional<TranslatedContent> getOrTranslate(TranslationContentType contentType, String contentId,
                                                        String originalText, String originalLang, String targetLang) {
        if (originalLang.equals(targetLang)) {
            return Optional.empty();
        }

        return translationCacheRepository.findByContentTypeAndContentIdAndTargetLang(contentType, contentId, targetLang)
                .map(cache -> new TranslatedContent(targetLang, cache.getTranslatedText()))
                .or(() -> {
                    String translatedText = stubTranslate(originalText, targetLang);
                    translationCacheRepository.save(new TranslationCache(contentType, contentId, targetLang,
                            translatedText, "stub-nllb-200-distilled"));
                    return Optional.of(new TranslatedContent(targetLang, translatedText));
                });
    }

    private String stubTranslate(String text, String targetLang) {
        return "[" + targetLang + "] " + text;
    }
}
