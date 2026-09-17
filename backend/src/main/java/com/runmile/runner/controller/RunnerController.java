package com.runmile.runner.controller;

import com.runmile.runner.dto.RunnerResponse;
import com.runmile.runner.service.RunnerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/runners")
public class RunnerController {
    private final RunnerService runnerService;

    public RunnerController(RunnerService runnerService) {
        this.runnerService = runnerService;
    }

    @GetMapping("/{runnerId}")
    public RunnerResponse getRunner(@PathVariable Long runnerId) {
        return runnerService.getRunner(runnerId);
    }
}
