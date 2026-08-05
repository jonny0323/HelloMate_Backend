package com.HelloMate.HelloMateBackend.domain.admin.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SemesterCalculatorTest {

    private final SemesterCalculator calculator = new SemesterCalculator(3, 9);

    @Test
    @DisplayName("3~8월은 해당 연도 1학기다")
    void firstSemester() {
        assertThat(calculator.currentSemesterStart(LocalDate.of(2026, 3, 1)))
                .isEqualTo(LocalDateTime.of(2026, 3, 1, 0, 0));
        assertThat(calculator.currentSemesterStart(LocalDate.of(2026, 8, 31)))
                .isEqualTo(LocalDateTime.of(2026, 3, 1, 0, 0));
    }

    @Test
    @DisplayName("9~12월은 해당 연도 2학기다")
    void secondSemester() {
        assertThat(calculator.currentSemesterStart(LocalDate.of(2026, 9, 1)))
                .isEqualTo(LocalDateTime.of(2026, 9, 1, 0, 0));
        assertThat(calculator.currentSemesterStart(LocalDate.of(2026, 12, 31)))
                .isEqualTo(LocalDateTime.of(2026, 9, 1, 0, 0));
    }

    @Test
    @DisplayName("1~2월은 직전 해 2학기로 잡는다 — 겨울방학에 학기가 바뀌면 통계가 초기화된다")
    void winterBelongsToPreviousSemester() {
        assertThat(calculator.currentSemesterStart(LocalDate.of(2026, 1, 15)))
                .isEqualTo(LocalDateTime.of(2025, 9, 1, 0, 0));
    }
}
