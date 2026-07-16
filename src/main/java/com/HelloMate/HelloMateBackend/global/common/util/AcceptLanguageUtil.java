package com.HelloMate.HelloMateBackend.global.common.util;

/**
 * Accept-Language 헤더(예: "ko-KR,ko;q=0.9,en;q=0.8")에서 번역 대상 언어 코드 하나만 추출한다.
 */
public final class AcceptLanguageUtil {

    private AcceptLanguageUtil() {
    }

    public static String primaryLanguage(String acceptLanguageHeader) {
        if (acceptLanguageHeader == null || acceptLanguageHeader.isBlank()) {
            return null;
        }
        String firstTag = acceptLanguageHeader.split(",")[0].split(";")[0].trim();
        int dashIndex = firstTag.indexOf('-');
        return (dashIndex > 0 ? firstTag.substring(0, dashIndex) : firstTag).toLowerCase();
    }
}
