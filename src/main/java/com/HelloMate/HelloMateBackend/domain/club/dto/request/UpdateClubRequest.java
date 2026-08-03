package com.HelloMate.HelloMateBackend.domain.club.dto.request;

import java.time.LocalDate;

public record UpdateClubRequest(String title, String introduction, Integer maxMembers, LocalDate deadline) {
}
