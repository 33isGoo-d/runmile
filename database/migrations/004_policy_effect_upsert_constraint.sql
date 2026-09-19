-- Existing Docker volumes need this key for policy-effect batch UPSERTs.
CREATE UNIQUE INDEX IF NOT EXISTS ux_policy_effect_scenario_scope
    ON policy_effect (scenario, scope_type, scope_value);
