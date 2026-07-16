package com.HelloMate.HelloMateBackend.domain.file.service;

import com.HelloMate.HelloMateBackend.domain.file.dto.response.PresignedUrlResponse;
import com.HelloMate.HelloMateBackend.domain.file.entity.UploadedFile;
import com.HelloMate.HelloMateBackend.domain.file.repository.UploadedFileRepository;
import com.HelloMate.HelloMateBackend.global.common.util.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalStubFileStorageService implements FileStorageService {

    private final UploadedFileRepository uploadedFileRepository;

    @Value("${hellomate.file.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public PresignedUrlResponse issuePresignedUrl(String filename, String contentType, String purpose) {
        String fileId = UuidCreator.create();
        String fileUrl = baseUrl + "/files/" + fileId + "/" + filename;
        String uploadUrl = baseUrl + "/uploads/" + fileId + "?stub-signature=" + UuidCreator.create();

        uploadedFileRepository.save(new UploadedFile(fileId, filename, contentType, purpose, fileUrl));

        return new PresignedUrlResponse(uploadUrl, fileId, fileUrl);
    }
}
