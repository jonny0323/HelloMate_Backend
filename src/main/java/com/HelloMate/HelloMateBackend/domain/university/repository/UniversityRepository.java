package com.HelloMate.HelloMateBackend.domain.university.repository;

import com.HelloMate.HelloMateBackend.domain.university.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, String> {
}
