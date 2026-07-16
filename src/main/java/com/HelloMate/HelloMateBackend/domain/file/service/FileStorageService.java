package com.HelloMate.HelloMateBackend.domain.file.service;

import com.HelloMate.HelloMateBackend.domain.file.dto.response.PresignedUrlResponse;

/**
 * 실제 서비스에서는 AWS S3 등으로 presigned URL을 발급하는 구현체로 교체한다.
 * 지금은 외부 인프라가 없어 로컬 스텁 구현체({@link LocalStubFileStorageService})만 제공한다.
 */
public interface FileStorageService {

    PresignedUrlResponse issuePresignedUrl(String filename, String contentType, String purpose);
}
