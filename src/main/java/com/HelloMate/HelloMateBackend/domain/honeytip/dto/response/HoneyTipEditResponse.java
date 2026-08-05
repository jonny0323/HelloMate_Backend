package com.HelloMate.HelloMateBackend.domain.honeytip.dto.response;

import com.HelloMate.HelloMateBackend.domain.honeytip.entity.EditRequestStatus;
import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTipEdit;

import java.time.LocalDateTime;

public record HoneyTipEditResponse(
        String id,
        String honeyTipId,
        String honeyTipTitle,
        String requesterName,
        String content,
        EditRequestStatus status,
        LocalDateTime createdAt
) {
    public static HoneyTipEditResponse from(HoneyTipEdit edit) {
        return new HoneyTipEditResponse(edit.getId(), edit.getHoneyTip().getId(), edit.getHoneyTip().getTitle(),
                edit.getRequester().getName(), edit.getContent(), edit.getStatus(), edit.getCreatedAt());
    }
}
