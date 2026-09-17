package com.runmile.analytics.dto;

import com.runmile.analytics.domain.PolicyEffect;

public record PolicyEffectResponse(
        String scopeType,
        String scopeValue,
        long actualSales,
        long predictedBaseline,
        long estimatedIncrementalSales,
        Double effectRatio
) {
    public static PolicyEffectResponse from(PolicyEffect effect) {
        return new PolicyEffectResponse(
                effect.getScopeType(),
                effect.getScopeValue(),
                effect.getActualSales(),
                effect.getPredictedBaseline(),
                effect.getEstimatedIncrementalSales(),
                effect.getEffectRatio()
        );
    }
}
