package com.runmile.analytics.dto;

import com.runmile.analytics.domain.PolicyEffect;
import com.runmile.global.type.Scenario;

public record AnalyticsOverviewResponse(
        Scenario scenario,
        long runmileBudget,
        long runmileUsed,
        long linkedPaymentAmount,
        long estimatedIncrementalSales,
        Double effectRatio
) {
    public static AnalyticsOverviewResponse from(PolicyEffect effect) {
        return new AnalyticsOverviewResponse(
                effect.getScenario(),
                effect.getRunmileBudget(),
                effect.getRunmileUsed(),
                effect.getLinkedPaymentAmount(),
                effect.getEstimatedIncrementalSales(),
                effect.getEffectRatio()
        );
    }
}
