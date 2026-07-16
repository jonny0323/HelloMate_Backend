package com.HelloMate.HelloMateBackend.domain.translation.controller;

import com.HelloMate.HelloMateBackend.domain.translation.dto.request.TranslateRequest;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.OfficialTranslateResponse;
import com.HelloMate.HelloMateBackend.domain.translation.dto.response.TranslateResponse;
import com.HelloMate.HelloMateBackend.domain.translation.service.TranslationService;
import com.HelloMate.HelloMateBackend.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translations")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping
    public ApiResponse<TranslateResponse> translate(@Valid @RequestBody TranslateRequest request) {
        return ApiResponse.ok(translationService.translate(request.text(), request.targetLang()));
    }

    @PostMapping("/official")
    public ApiResponse<OfficialTranslateResponse> translateOfficial(@Valid @RequestBody TranslateRequest request) {
        return ApiResponse.ok(translationService.translateOfficial(request.text(), request.targetLang()));
    }
}
