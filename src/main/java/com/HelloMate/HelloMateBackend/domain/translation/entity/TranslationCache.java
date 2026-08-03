package com.HelloMate.HelloMateBackend.domain.translation.entity;

import com.HelloMate.HelloMateBackend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 설계 문서 10장에서 제안한 번역 캐시. 공지/게시글 생성 시 비동기로 미리 번역해두고
 * 조회 API가 이 캐시를 우선 사용하도록 하는 구조를 지원한다.
 */
@Entity
@Table(name = "translation_cache", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"content_type", "content_id", "target_lang"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TranslationCache extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 30)
    private TranslationContentType contentType;

    @Column(name = "content_id", nullable = false, length = 255)
    private String contentId;

    @Column(name = "target_lang", nullable = false, length = 10)
    private String targetLang;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String translatedText;

    @Column(nullable = false, length = 50)
    private String model;

    public TranslationCache(TranslationContentType contentType, String contentId, String targetLang,
                             String translatedText, String model) {
        this.contentType = contentType;
        this.contentId = contentId;
        this.targetLang = targetLang;
        this.translatedText = translatedText;
        this.model = model;
    }
}
