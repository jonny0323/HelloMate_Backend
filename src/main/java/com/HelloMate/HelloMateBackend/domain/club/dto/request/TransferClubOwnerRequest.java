package com.HelloMate.HelloMateBackend.domain.club.dto.request;

import jakarta.validation.constraints.NotBlank;

/** 클럽장은 그냥 나갈 수 없어서, 나가려면 먼저 다른 멤버에게 위임해야 한다. */
public record TransferClubOwnerRequest(@NotBlank String newCreatorId) {
}
