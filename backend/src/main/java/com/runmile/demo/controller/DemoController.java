package com.runmile.demo.controller;

import com.runmile.demo.service.DemoResetService;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {
    private final DemoResetService demoResetService;

    public DemoController(DemoResetService demoResetService) {
        this.demoResetService = demoResetService;
    }

    @PostMapping("/reset")
    public Map<String, Boolean> reset() {
        demoResetService.reset();
        return Map.of("reset", true);
    }
}