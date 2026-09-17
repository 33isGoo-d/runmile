package com.runmile.runner.dto;

import com.runmile.global.type.Course;
import com.runmile.runner.domain.Runner;

public record RunnerResponse(Long id, String runnerCode, Course course) {
    public static RunnerResponse from(Runner runner) {
        return new RunnerResponse(runner.getId(), runner.getRunnerCode(), runner.getCourse());
    }
}
