package com.HelloMate.HelloMateBackend.domain.staff.dto.request;

import jakarta.validation.constraints.Size;

/** 부서는 초대 코드로 정해지므로 바꿀 수 없다(부서가 곧 공지 발신 권한이다). */
public record StaffProfileUpdateRequest(@Size(max = 100) String name, @Size(max = 100) String position) {
}
