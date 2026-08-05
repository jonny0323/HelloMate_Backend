package com.HelloMate.HelloMateBackend.domain.honeytip.dto.request;

import com.HelloMate.HelloMateBackend.domain.honeytip.dto.response.HoneyTipStep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateHoneyTipRequest(
        @NotBlank String category,
        @NotBlank String title,
        @NotBlank String content,
        @Size(max = 300) String tipMessage,
        List<HoneyTipStep> steps,
        @Size(max = 50) String estimatedFee,
        @Size(max = 50) String processingPeriod,
        @Size(max = 500) String externalLink
) {
}
