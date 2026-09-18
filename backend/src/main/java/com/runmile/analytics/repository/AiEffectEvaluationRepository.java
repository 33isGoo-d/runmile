package com.runmile.analytics.repository;

import com.runmile.analytics.domain.AiEffectEvaluation;
import com.runmile.global.type.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiEffectEvaluationRepository extends JpaRepository<AiEffectEvaluation, Scenario> {
}
