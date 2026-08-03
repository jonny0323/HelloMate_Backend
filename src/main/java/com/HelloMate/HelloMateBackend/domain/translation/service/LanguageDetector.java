package com.HelloMate.HelloMateBackend.domain.translation.service;

/**
 * 실제 NLLB-200 연동 전까지 사용하는 아주 단순한 유니코드 블록 기반 언어 감지 스텁.
 */
public final class LanguageDetector {

    private LanguageDetector() {
    }

    public static String detect(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (block == Character.UnicodeBlock.HANGUL_SYLLABLES
                    || block == Character.UnicodeBlock.HANGUL_JAMO
                    || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
                return "ko";
            }
            if (block == Character.UnicodeBlock.HIRAGANA || block == Character.UnicodeBlock.KATAKANA) {
                return "ja";
            }
            if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                return "zh";
            }
            if (block == Character.UnicodeBlock.CYRILLIC) {
                return "mn";
            }
        }
        return "en";
    }
}
