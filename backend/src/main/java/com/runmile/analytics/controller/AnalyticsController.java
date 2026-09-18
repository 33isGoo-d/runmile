package com.runmile.analytics.controller;

import com.runmile.analytics.dto.AnalyticsOverviewResponse;
import com.runmile.analytics.dto.AiEvaluationResponse;
import com.runmile.analytics.dto.CategoryAnalyticsResponse;
import com.runmile.analytics.dto.DistrictAnalyticsResponse;
import com.runmile.analytics.dto.InsightResponse;
import com.runmile.analytics.dto.PolicyEffectResponse;
import com.runmile.analytics.service.AnalyticsService;
import com.runmile.analytics.service.AiEvaluationService;
import com.runmile.global.type.Scenario;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final AiEvaluationService aiEvaluationService;

    public AnalyticsController(
            AnalyticsService analyticsService,
            AiEvaluationService aiEvaluationService
    ) {
        this.analyticsService = analyticsService;
        this.aiEvaluationService = aiEvaluationService;
    }

    @GetMapping("/overview")
    public AnalyticsOverviewResponse overview(
            @RequestParam(defaultValue = "MEDIUM") Scenario scenario
    ) {
        return analyticsService.getOverview(scenario);
    }

    @GetMapping("/districts")
    public List<DistrictAnalyticsResponse> districts(
            @RequestParam(defaultValue = "MEDIUM") Scenario scenario
    ) {
        return analyticsService.getDistricts(scenario);
    }

    @GetMapping("/categories")
    public List<CategoryAnalyticsResponse> categories() {
        return analyticsService.getCategories();
    }

    @GetMapping("/effects")
    public List<PolicyEffectResponse> effects(
            @RequestParam(defaultValue = "MEDIUM") Scenario scenario
    ) {
        return analyticsService.getEffects(scenario);
    }

    @GetMapping("/insights")
    public List<InsightResponse> insights(
            @RequestParam(defaultValue = "MEDIUM") Scenario scenario
    ) {
        return analyticsService.getInsights(scenario);
    }

    @GetMapping("/evaluation")
    public AiEvaluationResponse evaluation() {
        return aiEvaluationService.getEvaluation();
    }
}
