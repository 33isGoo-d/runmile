package com.runmile.completion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.runmile.completion.domain.Completion;
import com.runmile.completion.dto.CompletionResponse;
import com.runmile.completion.repository.CompletionRepository;
import com.runmile.global.ApiException;
import com.runmile.global.type.Course;
import com.runmile.runner.domain.Runner;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompletionServiceTests {
    private CompletionRepository completionRepository;
    private CompletionService completionService;

    @BeforeEach
    void setUp() {
        completionRepository = mock(CompletionRepository.class);
        completionService = new CompletionService(completionRepository);
    }

    @Test
    void 완주_기록을_조회한다() {
        Runner runner = mock(Runner.class);
        Completion completion = mock(Completion.class);
        when(runner.getCourse()).thenReturn(Course.FULL);
        when(completion.getRunner()).thenReturn(runner);
        when(completion.isCompleted()).thenReturn(true);
        when(completion.getFinishTimeSeconds()).thenReturn(12840);
        when(completion.getCompletedAt()).thenReturn(Instant.parse("2026-02-22T03:30:00Z"));
        when(completionRepository.findByRunnerId(1L)).thenReturn(Optional.of(completion));

        CompletionResponse response = completionService.getCompletion(1L);

        assertThat(response).isEqualTo(new CompletionResponse(
                true,
                Course.FULL,
                12840,
                LocalDateTime.parse("2026-02-22T12:30:00")
        ));
    }

    @Test
    void 완주_기록이_없으면_404_오류를_반환한다() {
        when(completionRepository.findByRunnerId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completionService.getCompletion(999L))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.status().value()).isEqualTo(404);
                    assertThat(exception.code()).isEqualTo("COMPLETION_NOT_FOUND");
                    assertThat(exception.getMessage()).isEqualTo("완주 기록을 찾을 수 없습니다.");
                });
    }
}
