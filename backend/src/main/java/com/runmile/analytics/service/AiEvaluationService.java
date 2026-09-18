package com.runmile.analytics.service;

import com.runmile.analytics.domain.AiEffectEvaluation;
import com.runmile.analytics.domain.AiModelMetric;
import com.runmile.analytics.dto.AiEvaluationResponse;
import com.runmile.analytics.dto.AiEvaluationResponse.BaselineMetricResponse;
import com.runmile.analytics.dto.AiEvaluationResponse.EffectEvaluationResponse;
import com.runmile.analytics.repository.AiEffectEvaluationRepository;
import com.runmile.analytics.repository.AiModelMetricRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiEvaluationService {
    private final AiModelMetricRepository modelMetricRepository;
    private final AiEffectEvaluationRepository effectEvaluationRepository;

    public AiEvaluationService(
            AiModelMetricRepository modelMetricRepository,
            AiEffectEvaluationRepository effectEvaluationRepository
    ) {
        this.modelMetricRepository = modelMetricRepository;
        this.effectEvaluationRepository = effectEvaluationRepository;
    }

    @Transactional(readOnly = true)
    public AiEvaluationResponse getEvaluation() {
        Map<String, AiModelMetric> metrics = modelMetricRepository
                .findAllById(List.of("MAE", "MAPE", "RMSE"))
                .stream()
                .collect(Collectors.toMap(AiModelMetric::getMetricName, metric -> metric));
        BaselineMetricResponse baseline = createBaseline(metrics);
        List<EffectEvaluationResponse> effects = effectEvaluationRepository.findAll().stream()
                .sorted(Comparator.comparingInt(effect -> effect.getScenario().ordinal()))
                .map(this::toResponse)
                .toList();
        return new AiEvaluationResponse(baseline, effects);
    }

    private BaselineMetricResponse createBaseline(Map<String, AiModelMetric> metrics) {
        if (!metrics.keySet().containsAll(List.of("MAE", "MAPE", "RMSE"))) {
            return null;
        }
        Instant evaluatedAt = metrics.values().stream()
                .map(AiModelMetric::getEvaluatedAt)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        return new BaselineMetricResponse(
                metrics.get("MAE").getMetricValue(),
                metrics.get("MAPE").getMetricValue(),
                metrics.get("RMSE").getMetricValue(),
                evaluatedAt
        );
    }

    private EffectEvaluationResponse toResponse(AiEffectEvaluation evaluation) {
        return new EffectEvaluationResponse(
                evaluation.getScenario(),
                evaluation.getInjectedEffect(),
                evaluation.getEstimatedEffect(),
                evaluation.getDifference(),
                evaluation.getDifferencePct(),
                evaluation.getEvaluatedAt()
        );
    }
}
