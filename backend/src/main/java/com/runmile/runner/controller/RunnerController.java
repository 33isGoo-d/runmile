package com.runmile.runner.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/runners")
public class RunnerController {
    @GetMapping("/{runnerId}")
    public Map<String, Object> getRunner(@PathVariable Long runnerId) {
        return Map.of(
                "id", runnerId,
                "runnerCode", "RUNNER_%05d".formatted(runnerId),
                "course", "FULL"
        );
    }
}
