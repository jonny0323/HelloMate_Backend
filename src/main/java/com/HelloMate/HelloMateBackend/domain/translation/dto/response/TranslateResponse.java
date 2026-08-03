package com.HelloMate.HelloMateBackend.domain.translation.dto.response;

public record TranslateResponse(String translatedText, String detectedSourceLang, String model) {
}
