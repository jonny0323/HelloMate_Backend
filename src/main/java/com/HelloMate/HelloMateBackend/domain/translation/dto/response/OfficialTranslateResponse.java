package com.HelloMate.HelloMateBackend.domain.translation.dto.response;

public record OfficialTranslateResponse(String translatedText, String model, boolean needsHumanReview, double confidence) {
}
