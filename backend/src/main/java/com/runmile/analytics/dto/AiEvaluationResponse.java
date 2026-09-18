package com.runmile.analytics.dto;

import com.runmile.global.type.Scenario;
import java.time.Instant;
import java.util.List;

public record AiEvaluationResponse(
        BaselineMetricResponse baseline,
        List<EffectEvaluationResponse> effects
) {
    public record BaselineMetricResponse(
            double mae,
            double mape,
            double rmse,
            Instant evaluatedAt
    ) {
    }

    public record EffectEvaluationResponse(
            Scenario scenario,
            long injectedEffect,
            long estimatedEffect,
            long difference,
            Double differencePct,
            Instant evaluatedAt
    ) {
    }
}
