package com.HelloMate.HelloMateBackend.domain.honeytip.dto.response;

import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTip;

import java.time.LocalDateTime;
import java.util.List;

public record HoneyTipResponse(
        String id,
        String category,
        String title,
        String content,
        String tipMessage,
        List<HoneyTipStep> steps,
        String estimatedFee,
        String processingPeriod,
        String externalLink,
        String disclaimer,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 비자·보험처럼 틀리면 체류자격까지 걸리는 정보를 다루므로 항상 면책 문구를 함께 내려보낸다.
     * 디자인에는 없지만 클라이언트가 상시 노출해야 한다.
     */
    public static final String DISCLAIMER = "본 정보는 참고용이며, 최신 정보는 관할 기관에 확인해 주세요.";

    public static HoneyTipResponse of(HoneyTip honeyTip, List<HoneyTipStep> steps) {
        return new HoneyTipResponse(honeyTip.getId(), honeyTip.getCategory(), honeyTip.getTitle(),
                honeyTip.getContent(), honeyTip.getTipMessage(), steps, honeyTip.getEstimatedFee(),
                honeyTip.getProcessingPeriod(), honeyTip.getExternalLink(), DISCLAIMER,
                honeyTip.getViewCount(), honeyTip.getCreatedAt(), honeyTip.getUpdatedAt());
    }
}
