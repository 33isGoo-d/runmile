package com.runmile.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ai_model_metric")
public class AiModelMetric {
    @Id
    @Column(name = "metric_name", length = 16)
    private String metricName;

    @Column(name = "metric_value", nullable = false)
    private double metricValue;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    protected AiModelMetric() {
    }

    public String getMetricName() {
        return metricName;
    }

    public double getMetricValue() {
        return metricValue;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }
}
