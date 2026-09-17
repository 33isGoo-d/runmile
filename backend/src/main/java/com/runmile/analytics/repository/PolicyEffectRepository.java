package com.runmile.analytics.repository;

import com.runmile.analytics.domain.PolicyEffect;
import com.runmile.global.type.Scenario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyEffectRepository extends JpaRepository<PolicyEffect, Long> {
    Optional<PolicyEffect> findFirstByScenarioAndScopeTypeAndScopeValueOrderByIdDesc(
            Scenario scenario,
            String scopeType,
            String scopeValue
    );

    List<PolicyEffect> findAllByScenarioAndScopeTypeOrderByEstimatedIncrementalSalesDesc(
            Scenario scenario,
            String scopeType
    );
}
