package com.HelloMate.HelloMateBackend.domain.staff.repository;

import com.HelloMate.HelloMateBackend.domain.staff.entity.StaffInviteCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffInviteCodeRepository extends JpaRepository<StaffInviteCode, String> {

    Optional<StaffInviteCode> findByCode(String code);
}
