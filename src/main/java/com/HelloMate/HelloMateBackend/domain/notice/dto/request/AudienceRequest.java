package com.HelloMate.HelloMateBackend.domain.notice.dto.request;

import com.HelloMate.HelloMateBackend.domain.notice.entity.AudienceMode;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 담당자 콘솔의 '받는 사람' 3모드.
 *
 * GROUP 모드는 국가와 학과가 서로 다른 축이라 필드를 나눴다. 한 리스트에 섞으면 "베트남 + 컴퓨터공학과"가
 * 합집합인지 교집합인지 알 수 없다.
 * 규칙: <b>축 안에서는 OR, 축 사이에서는 AND</b> — 둘 다 주면 "베트남 국적이면서 컴퓨터공학과"다.
 */
public record AudienceRequest(
        @NotNull AudienceMode mode,
        List<String> countryCodes,
        List<String> majors,
        List<String> studentIds
) {
    public boolean hasCountryFilter() {
        return countryCodes != null && !countryCodes.isEmpty();
    }

    public boolean hasMajorFilter() {
        return majors != null && !majors.isEmpty();
    }
}
