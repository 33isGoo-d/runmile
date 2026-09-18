package com.runmile.analytics.repository;

import com.runmile.analytics.domain.AiModelMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiModelMetricRepository extends JpaRepository<AiModelMetric, String> {
}
