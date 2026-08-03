package com.HelloMate.HelloMateBackend.domain.file.repository;

import com.HelloMate.HelloMateBackend.domain.file.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, String> {
}
