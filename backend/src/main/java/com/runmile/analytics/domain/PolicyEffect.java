package com.runmile.analytics.domain;

import com.runmile.global.type.Scenario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "policy_effect")
public class PolicyEffect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Scenario scenario;

    @Column(name = "scope_type", nullable = false, length = 32)
    private String scopeType;

    @Column(name = "scope_value", nullable = false, length = 80)
    private String scopeValue;

    @Column(name = "runmile_budget", nullable = false)
    private long runmileBudget;

    @Column(name = "runmile_used", nullable = false)
    private long runmileUsed;

    @Column(name = "linked_payment_amount", nullable = false)
    private long linkedPaymentAmount;

    @Column(name = "actual_sales", nullable = false)
    private long actualSales;

    @Column(name = "predicted_baseline", nullable = false)
    private long predictedBaseline;

    @Column(name = "estimated_incremental_sales", nullable = false)
    private long estimatedIncrementalSales;

    @Column(name = "effect_ratio")
    private Double effectRatio;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PolicyEffect() {
    }

    public Scenario getScenario() {
        return scenario;
    }

    public String getScopeType() {
        return scopeType;
    }

    public String getScopeValue() {
        return scopeValue;
    }

    public long getRunmileBudget() {
        return runmileBudget;
    }

    public long getRunmileUsed() {
        return runmileUsed;
    }

    public long getLinkedPaymentAmount() {
        return linkedPaymentAmount;
    }

    public long getActualSales() {
        return actualSales;
    }

    public long getPredictedBaseline() {
        return predictedBaseline;
    }

    public long getEstimatedIncrementalSales() {
        return estimatedIncrementalSales;
    }

    public Double getEffectRatio() {
        return effectRatio;
    }
}
