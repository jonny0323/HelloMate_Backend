package com.HelloMate.HelloMateBackend.domain.translation.repository;

import com.HelloMate.HelloMateBackend.domain.translation.entity.TranslationCache;
import com.HelloMate.HelloMateBackend.domain.translation.entity.TranslationContentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TranslationCacheRepository extends JpaRepository<TranslationCache, Long> {

    Optional<TranslationCache> findByContentTypeAndContentIdAndTargetLang(
            TranslationContentType contentType, String contentId, String targetLang);
}
