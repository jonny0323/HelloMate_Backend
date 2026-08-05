package com.HelloMate.HelloMateBackend.domain.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 대시보드의 "이번 학기" 경계를 계산한다.
 *
 * 학사일정 테이블을 만들 만큼 근거가 쌓이지 않아서 설정값으로 뺐다. 학교마다 개강월이 다르면
 * application.yaml만 바꾸면 되고, 나중에 학사일정 도메인이 생기면 이 클래스만 갈아끼우면 된다.
 */
@Component
public class SemesterCalculator {

    private final int firstSemesterStartMonth;
    private final int secondSemesterStartMonth;

    public SemesterCalculator(
            @Value("${hellomate.academic.first-semester-start-month:3}") int firstSemesterStartMonth,
            @Value("${hellomate.academic.second-semester-start-month:9}") int secondSemesterStartMonth) {
        this.firstSemesterStartMonth = firstSemesterStartMonth;
        this.secondSemesterStartMonth = secondSemesterStartMonth;
    }

    public LocalDateTime currentSemesterStart() {
        return currentSemesterStart(LocalDate.now());
    }

    LocalDateTime currentSemesterStart(LocalDate today) {
        int month = today.getMonthValue();
        if (month >= secondSemesterStartMonth) {
            return LocalDate.of(today.getYear(), secondSemesterStartMonth, 1).atStartOfDay();
        }
        if (month >= firstSemesterStartMonth) {
            return LocalDate.of(today.getYear(), firstSemesterStartMonth, 1).atStartOfDay();
        }
        // 1~2월은 직전 해 2학기(겨울학기)에 속한다.
        return LocalDate.of(today.getYear() - 1, secondSemesterStartMonth, 1).atStartOfDay();
    }
}
