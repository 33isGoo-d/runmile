package com.runmile.analytics.domain;

import com.runmile.global.type.Scenario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ai_effect_evaluation")
public class AiEffectEvaluation {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Scenario scenario;

    @Column(name = "injected_effect", nullable = false)
    private long injectedEffect;

    @Column(name = "estimated_effect", nullable = false)
    private long estimatedEffect;

    @Column(nullable = false)
    private long difference;

    @Column(name = "difference_pct")
    private Double differencePct;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    protected AiEffectEvaluation() {
    }

    public Scenario getScenario() {
        return scenario;
    }

    public long getInjectedEffect() {
        return injectedEffect;
    }

    public long getEstimatedEffect() {
        return estimatedEffect;
    }

    public long getDifference() {
        return difference;
    }

    public Double getDifferencePct() {
        return differencePct;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }
}
