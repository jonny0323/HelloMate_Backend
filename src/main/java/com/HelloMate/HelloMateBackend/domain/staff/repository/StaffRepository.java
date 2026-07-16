package com.HelloMate.HelloMateBackend.domain.staff.repository;

import com.HelloMate.HelloMateBackend.domain.staff.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, String> {

    boolean existsByEmail(String email);

    Optional<Staff> findByEmail(String email);
}
