package com.runmile.demo.controller;

import com.runmile.demo.dto.DemoResetRequest;
import com.runmile.demo.service.DemoResetService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
@ConditionalOnProperty(prefix = "runmile.demo-reset", name = "enabled", havingValue = "true")
public class DemoController {
    private final DemoResetService demoResetService;

    public DemoController(DemoResetService demoResetService) {
        this.demoResetService = demoResetService;
    }

    @PostMapping("/reset")
    public Map<String, Boolean> reset(@Valid @RequestBody DemoResetRequest request) {
        demoResetService.reset();
        return Map.of("reset", true);
    }
}
