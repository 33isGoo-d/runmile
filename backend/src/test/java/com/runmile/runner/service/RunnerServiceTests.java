package com.runmile.runner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.runmile.global.ApiException;
import com.runmile.global.type.Course;
import com.runmile.runner.domain.Runner;
import com.runmile.runner.dto.RunnerResponse;
import com.runmile.runner.repository.RunnerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RunnerServiceTests {
    private RunnerRepository runnerRepository;
    private RunnerService runnerService;

    @BeforeEach
    void setUp() {
        runnerRepository = mock(RunnerRepository.class);
        runnerService = new RunnerService(runnerRepository);
    }

    @Test
    void 참가자를_조회한다() {
        Runner runner = mock(Runner.class);
        when(runner.getId()).thenReturn(1L);
        when(runner.getRunnerCode()).thenReturn("RUNNER_00001");
        when(runner.getCourse()).thenReturn(Course.FULL);
        when(runnerRepository.findById(1L)).thenReturn(Optional.of(runner));

        RunnerResponse response = runnerService.getRunner(1L);

        assertThat(response).isEqualTo(new RunnerResponse(1L, "RUNNER_00001", Course.FULL));
    }

    @Test
    void 존재하지_않는_참가자는_404_오류를_반환한다() {
        when(runnerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> runnerService.getRunner(999L))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.status().value()).isEqualTo(404);
                    assertThat(exception.code()).isEqualTo("RUNNER_NOT_FOUND");
                    assertThat(exception.getMessage()).isEqualTo("참가자를 찾을 수 없습니다.");
                });
    }
}
