package com.HelloMate.HelloMateBackend.domain.honeytip.service;

import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipStep;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * honey_tip.steps_json 컬럼과 STEP 리스트 사이의 변환만 담당한다.
 * 파싱이 깨져도 정보글 본문은 보여줄 수 있어야 하므로 예외를 던지지 않고 빈 목록으로 떨어뜨린다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HoneyTipStepCodec {

    private static final TypeReference<List<HoneyTipStep>> STEP_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public String encode(List<HoneyTipStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(steps);
        } catch (Exception e) {
            log.error("정보글 STEP 직렬화 실패", e);
            return null;
        }
    }

    public List<HoneyTipStep> decode(String stepsJson) {
        if (stepsJson == null || stepsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(stepsJson, STEP_LIST);
        } catch (Exception e) {
            log.error("정보글 STEP 파싱 실패. json={}", stepsJson, e);
            return List.of();
        }
    }
}
