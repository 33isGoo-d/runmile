package com.runmile.runner.service;

import com.runmile.global.ApiException;
import com.runmile.runner.domain.Runner;
import com.runmile.runner.dto.RunnerResponse;
import com.runmile.runner.repository.RunnerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RunnerService {
    private final RunnerRepository runnerRepository;

    public RunnerService(RunnerRepository runnerRepository) {
        this.runnerRepository = runnerRepository;
    }

    @Transactional(readOnly = true)
    public RunnerResponse getRunner(Long runnerId) {
        Runner runner = runnerRepository.findById(runnerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "RUNNER_NOT_FOUND",
                        "참가자를 찾을 수 없습니다."
                ));

        return RunnerResponse.from(runner);
    }
}
