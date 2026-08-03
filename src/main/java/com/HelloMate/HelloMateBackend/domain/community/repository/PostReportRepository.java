package com.HelloMate.HelloMateBackend.domain.community.repository;

import com.HelloMate.HelloMateBackend.domain.community.entity.PostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport, String> {

    Page<PostReport> findByPostIdIsNotNull(Pageable pageable);
}
