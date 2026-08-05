package com.HelloMate.HelloMateBackend.domain.staff.service;

import com.HelloMate.HelloMateBackend.domain.staff.dto.request.StaffProfileUpdateRequest;
import com.HelloMate.HelloMateBackend.domain.staff.dto.response.StaffProfileResponse;
import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import com.HelloMate.HelloMateBackend.domain.staff.repository.StaffRepository;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffProfileResponse getMyProfile(String staffId) {
        return StaffProfileResponse.from(getStaff(staffId));
    }

    @Transactional
    public StaffProfileResponse updateMyProfile(String staffId, StaffProfileUpdateRequest request) {
        Staff staff = getStaff(staffId);
        staff.updateProfile(request.name(), request.position());
        return StaffProfileResponse.from(staff);
    }

    public Staff getStaff(String staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAFF_NOT_FOUND));
    }
}
