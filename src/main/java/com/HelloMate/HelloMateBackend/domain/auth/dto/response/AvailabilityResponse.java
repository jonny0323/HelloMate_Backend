package com.HelloMate.HelloMateBackend.domain.auth.dto.response;

/**
 * 회원가입 1/3의 [중복 확인]은 사용 가능할 때도 문구("사용할 수 있는 아이디예요")를 보여준다.
 * 중복이면 409로 떨어지므로 이 응답은 사용 가능한 경우에만 내려간다.
 */
public record AvailabilityResponse(boolean available, String message) {

    public static AvailabilityResponse available(String message) {
        return new AvailabilityResponse(true, message);
    }
}
