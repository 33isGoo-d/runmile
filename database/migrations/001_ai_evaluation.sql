CREATE TABLE IF NOT EXISTS ai_model_metric (
    metric_name VARCHAR(16) PRIMARY KEY CHECK (metric_name IN ('MAE', 'MAPE', 'RMSE')),
    metric_value DOUBLE PRECISION NOT NULL CHECK (metric_value >= 0),
    evaluated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS ai_effect_evaluation (
    scenario VARCHAR(16) PRIMARY KEY CHECK (scenario IN ('NONE', 'LOW', 'MEDIUM', 'HIGH')),
    injected_effect BIGINT NOT NULL,
    estimated_effect BIGINT NOT NULL,
    difference BIGINT NOT NULL,
    difference_pct DOUBLE PRECISION,
    evaluated_at TIMESTAMPTZ NOT NULL
);
