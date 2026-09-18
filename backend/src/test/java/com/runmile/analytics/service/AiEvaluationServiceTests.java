package com.runmile.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.runmile.analytics.domain.AiEffectEvaluation;
import com.runmile.analytics.domain.AiModelMetric;
import com.runmile.analytics.dto.AiEvaluationResponse;
import com.runmile.analytics.repository.AiEffectEvaluationRepository;
import com.runmile.analytics.repository.AiModelMetricRepository;
import com.runmile.global.type.Scenario;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiEvaluationServiceTests {
    @Test
    void 평가_지표가_없으면_빈_응답을_반환한다() {
        AiModelMetricRepository modelRepository = mock(AiModelMetricRepository.class);
        AiEffectEvaluationRepository effectRepository = mock(AiEffectEvaluationRepository.class);
        when(modelRepository.findAll()).thenReturn(List.of());
        when(effectRepository.findAll()).thenReturn(List.of());

        AiEvaluationResponse result = new AiEvaluationService(
                modelRepository,
                effectRepository
        ).getEvaluation();

        assertThat(result.baseline()).isNull();
        assertThat(result.effects()).isEmpty();
    }

    @Test
    void 모델과_효과_평가_지표를_시나리오_순서로_반환한다() {
        AiModelMetricRepository modelRepository = mock(AiModelMetricRepository.class);
        AiEffectEvaluationRepository effectRepository = mock(AiEffectEvaluationRepository.class);
        Instant evaluatedAt = Instant.parse("2026-09-18T00:00:00Z");
        AiModelMetric rmse = metric("RMSE", 120_000, evaluatedAt);
        AiModelMetric mae = metric("MAE", 80_000, evaluatedAt);
        AiModelMetric mape = metric("MAPE", 0.08, evaluatedAt);
        AiEffectEvaluation high = effect(
                Scenario.HIGH, 67_000_000, 65_000_000, -2_000_000, -3.0, evaluatedAt
        );
        AiEffectEvaluation none = effect(
                Scenario.NONE, 0, -1_000_000, -1_000_000, null, evaluatedAt
        );
        when(modelRepository.findAll()).thenReturn(List.of(
                rmse,
                mae,
                mape
        ));
        when(effectRepository.findAll()).thenReturn(List.of(
                high,
                none
        ));

        AiEvaluationResponse result = new AiEvaluationService(
                modelRepository,
                effectRepository
        ).getEvaluation();

        assertThat(result.baseline().mae()).isEqualTo(80_000);
        assertThat(result.baseline().mape()).isEqualTo(0.08);
        assertThat(result.baseline().rmse()).isEqualTo(120_000);
        assertThat(result.effects()).extracting(AiEvaluationResponse.EffectEvaluationResponse::scenario)
                .containsExactly(Scenario.NONE, Scenario.HIGH);
    }

    private AiModelMetric metric(String name, double value, Instant evaluatedAt) {
        AiModelMetric metric = mock(AiModelMetric.class);
        when(metric.getMetricName()).thenReturn(name);
        when(metric.getMetricValue()).thenReturn(value);
        when(metric.getEvaluatedAt()).thenReturn(evaluatedAt);
        return metric;
    }

    private AiEffectEvaluation effect(
            Scenario scenario,
            long injected,
            long estimated,
            long difference,
            Double differencePct,
            Instant evaluatedAt
    ) {
        AiEffectEvaluation effect = mock(AiEffectEvaluation.class);
        when(effect.getScenario()).thenReturn(scenario);
        when(effect.getInjectedEffect()).thenReturn(injected);
        when(effect.getEstimatedEffect()).thenReturn(estimated);
        when(effect.getDifference()).thenReturn(difference);
        when(effect.getDifferencePct()).thenReturn(differencePct);
        when(effect.getEvaluatedAt()).thenReturn(evaluatedAt);
        return effect;
    }
}
