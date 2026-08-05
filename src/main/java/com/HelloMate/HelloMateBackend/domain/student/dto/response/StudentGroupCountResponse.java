package com.HelloMate.HelloMateBackend.domain.student.dto.response;

/**
 * 국가/학과별 재학 학생 수 집계 결과. 공지 대상 그룹 카드에 쓰인다.
 * count가 Long인 이유: JPQL 생성자 표현식이 넘겨주는 count(s)의 타입이 Long이라 primitive면 매칭이 깨진다.
 */
public record StudentGroupCountResponse(String groupKey, Long count) {
}
