package com.HelloMate.HelloMateBackend.domain.auth.dto.request;

import com.HelloMate.HelloMateBackend.domain.student.entity.StudentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 회원가입 3 STEP을 마지막 [인증하기]에서 한 번에 제출한다.
 * (STEP 사이 [이전] 버튼으로 값이 보존되어야 해서 중간 저장을 두지 않았다.)
 */
public record StudentSignUpRequest(
        @NotBlank
        @Pattern(regexp = "^[a-z0-9_]{4,20}$", message = "아이디는 영문 소문자, 숫자, _ 조합 4~20자입니다.")
        String loginId,

        @NotBlank @Email String email,
        @NotBlank String name,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다.")
        String password,

        @NotBlank String country,
        @NotBlank String language,
        @NotNull StudentType studentType,
        String major,
        String grade,
        @Min(1900) @Max(2100) Integer birthYear,
        @NotBlank String universityId,

        @AssertTrue(message = "서비스 이용약관에 동의해 주세요.") boolean termsAgreed,
        @AssertTrue(message = "개인정보 수집 및 이용에 동의해 주세요.") boolean privacyAgreed
) {
}
