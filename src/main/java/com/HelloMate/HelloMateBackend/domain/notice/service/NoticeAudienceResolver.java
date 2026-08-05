package com.HelloMate.HelloMateBackend.domain.notice.service;

import com.HelloMate.HelloMateBackend.domain.notice.dto.request.AudienceRequest;
import com.HelloMate.HelloMateBackend.domain.student.entity.Student;
import com.HelloMate.HelloMateBackend.domain.student.repository.StudentRepository;
import com.HelloMate.HelloMateBackend.global.common.exception.BusinessException;
import com.HelloMate.HelloMateBackend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 공지 수신 대상 해석만 담당한다. 발송 전 인원 미리보기와 실제 발송이 같은 규칙을 타야 해서
 * NoticeService에서 떼어냈다 — 두 곳에 로직이 갈라지면 "312명에게 갑니다"와 실제 수신자가 어긋난다.
 *
 * 그룹 규칙: 축(국가/학과) 안에서는 OR, 축 사이에서는 AND.
 */
@Component
@RequiredArgsConstructor
public class NoticeAudienceResolver {

    private final StudentRepository studentRepository;

    public List<Student> resolve(String universityId, AudienceRequest audience) {
        List<Student> recipients = switch (audience.mode()) {
            case ALL -> studentRepository.findAudienceAll(universityId);
            case GROUP -> resolveGroup(universityId, audience);
            case INDIVIDUAL -> audience.studentIds() == null || audience.studentIds().isEmpty()
                    ? List.of()
                    : studentRepository.findAudienceByIds(audience.studentIds());
        };

        // 조건에 맞는 학생이 없는데도 발송에 성공하면 담당자는 보냈다고 믿는다. 명시적으로 막는다.
        if (recipients.isEmpty()) {
            throw new BusinessException(ErrorCode.NOTICE_AUDIENCE_EMPTY);
        }
        return recipients;
    }

    /** 발송함/작성 화면에 그대로 노출되는 대상 설명. 발송 시점 조건을 문자열로 굳혀 둔다. */
    public String describe(AudienceRequest audience, int recipientCount) {
        return switch (audience.mode()) {
            case ALL -> "전체 학생";
            case INDIVIDUAL -> recipientCount + "명 개별 선택";
            case GROUP -> {
                List<String> parts = new ArrayList<>();
                if (audience.hasCountryFilter()) {
                    parts.add(String.join(", ", audience.countryCodes()));
                }
                if (audience.hasMajorFilter()) {
                    parts.add(String.join(", ", audience.majors()));
                }
                yield parts.isEmpty() ? "전체 학생" : String.join(" · ", parts);
            }
        };
    }

    private List<Student> resolveGroup(String universityId, AudienceRequest audience) {
        boolean byCountry = audience.hasCountryFilter();
        boolean byMajor = audience.hasMajorFilter();

        if (byCountry && byMajor) {
            return studentRepository.findAudienceByCountryAndMajor(universityId, upperCase(audience.countryCodes()), audience.majors());
        }
        if (byCountry) {
            return studentRepository.findAudienceByCountry(universityId, upperCase(audience.countryCodes()));
        }
        if (byMajor) {
            return studentRepository.findAudienceByMajor(universityId, audience.majors());
        }
        // GROUP인데 아무 조건도 없으면 전체 발송이 되어버린다. 실수로 312명에게 나가는 걸 막는다.
        throw new BusinessException(ErrorCode.INVALID_INPUT, "대상 그룹을 하나 이상 선택해 주세요.");
    }

    private List<String> upperCase(List<String> values) {
        return values.stream().map(v -> v.toUpperCase(Locale.ROOT)).toList();
    }
}
