package com.HelloMate.HelloMateBackend.domain.university.service;

import com.HelloMate.HelloMateBackend.domain.university.dto.response.MajorResponse;
import com.HelloMate.HelloMateBackend.domain.university.repository.MajorRepository;
import com.HelloMate.HelloMateBackend.domain.university.repository.UniversityRepository;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MajorService {

    private final MajorRepository majorRepository;
    private final UniversityRepository universityRepository;

    public List<MajorResponse> search(String universityId, String query) {
        if (!universityRepository.existsById(universityId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "존재하지 않는 학교입니다.");
        }
        return majorRepository.search(universityId, query).stream()
                .map(MajorResponse::from)
                .toList();
    }
}
