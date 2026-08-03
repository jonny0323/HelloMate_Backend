package com.HelloMate.HelloMateBackend.domain.honeytip.repository;

import com.HelloMate.HelloMateBackend.domain.honeytip.entity.HoneyTipEdit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoneyTipEditRepository extends JpaRepository<HoneyTipEdit, String> {

    List<HoneyTipEdit> findByHoneyTipIdOrderByCreatedAtDesc(String honeyTipId);
}
