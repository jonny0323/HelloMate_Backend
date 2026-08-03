package com.HelloMate.HelloMateBackend.domain.staff.dto.response;

import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;

public record StaffProfileResponse(
        String id,
        String email,
        String name,
        String position,
        String department,
        boolean verified,
        String universityName
) {
    public static StaffProfileResponse from(Staff staff) {
        return new StaffProfileResponse(
                staff.getId(),
                staff.getEmail(),
                staff.getName(),
                staff.getPosition(),
                staff.getDepartment(),
                staff.isVerified(),
                staff.getUniversity().getName()
        );
    }
}
