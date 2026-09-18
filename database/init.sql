CREATE TABLE runner (
    id BIGSERIAL PRIMARY KEY,
    runner_code VARCHAR(64) NOT NULL UNIQUE,
    course VARCHAR(16) NOT NULL CHECK (course IN ('FULL', 'TEN_K', 'FIVE_K')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE completion (
    id BIGSERIAL PRIMARY KEY,
    runner_id BIGINT NOT NULL UNIQUE REFERENCES runner(id),
    finish_time_seconds INTEGER,
    completed BOOLEAN NOT NULL DEFAULT false,
    completed_at TIMESTAMPTZ
);

CREATE TABLE nft_record (
    id BIGSERIAL PRIMARY KEY,
    runner_id BIGINT NOT NULL UNIQUE REFERENCES runner(id),
    completion_id BIGINT NOT NULL UNIQUE REFERENCES completion(id),
    nft_token_id VARCHAR(128) NOT NULL UNIQUE,
    network VARCHAR(64) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT false,
    issued_at TIMESTAMPTZ
);

CREATE TABLE runmile_wallet (
    id BIGSERIAL PRIMARY KEY,
    runner_id BIGINT NOT NULL UNIQUE REFERENCES runner(id),
    balance BIGINT NOT NULL DEFAULT 0 CHECK (balance >= 0),
    total_issued BIGINT NOT NULL DEFAULT 0 CHECK (total_issued >= 0),
    total_used BIGINT NOT NULL DEFAULT 0 CHECK (total_used >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE merchant (
    id BIGSERIAL PRIMARY KEY,
    merchant_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    district VARCHAR(80) NOT NULL,
    category VARCHAR(32) NOT NULL CHECK (category IN ('RESTAURANT', 'CAFE', 'RETAIL', 'ACCOMMODATION', 'OTHER')),
    address VARCHAR(255),
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    runmile_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    runner_id BIGINT NOT NULL REFERENCES runner(id),
    merchant_id BIGINT NOT NULL REFERENCES merchant(id),
    total_amount BIGINT NOT NULL CHECK (total_amount > 0),
    runmile_amount BIGINT NOT NULL CHECK (runmile_amount >= 0),
    personal_amount BIGINT NOT NULL CHECK (personal_amount >= 0),
    status VARCHAR(16) NOT NULL CHECK (status IN ('SUCCESS', 'CANCELLED')),
    paid_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (total_amount = runmile_amount + personal_amount)
);

CREATE TABLE runmile_transaction (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES runmile_wallet(id),
    type VARCHAR(16) NOT NULL CHECK (type IN ('ISSUE', 'USE', 'CANCEL')),
    amount BIGINT NOT NULL CHECK (amount > 0),
    payment_id BIGINT REFERENCES payment(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE merchant_daily_sales (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES merchant(id),
    date DATE NOT NULL,
    sales_amount BIGINT NOT NULL CHECK (sales_amount >= 0),
    transaction_count INTEGER NOT NULL CHECK (transaction_count >= 0),
    temperature DOUBLE PRECISION,
    rainfall DOUBLE PRECISION,
    is_weekend BOOLEAN NOT NULL,
    is_marathon_day BOOLEAN NOT NULL,
    scenario VARCHAR(16) NOT NULL CHECK (scenario IN ('NONE', 'LOW', 'MEDIUM', 'HIGH')),
    UNIQUE (merchant_id, date, scenario)
);

CREATE TABLE ai_prediction (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES merchant(id),
    date DATE NOT NULL,
    scenario VARCHAR(16) NOT NULL CHECK (scenario IN ('NONE', 'LOW', 'MEDIUM', 'HIGH')),
    actual_sales BIGINT NOT NULL CHECK (actual_sales >= 0),
    predicted_baseline BIGINT NOT NULL CHECK (predicted_baseline >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (merchant_id, date, scenario)
);

CREATE TABLE policy_effect (
    id BIGSERIAL PRIMARY KEY,
    scenario VARCHAR(16) NOT NULL CHECK (scenario IN ('NONE', 'LOW', 'MEDIUM', 'HIGH')),
    scope_type VARCHAR(32) NOT NULL CHECK (scope_type IN ('TOTAL', 'DISTRICT', 'CATEGORY')),
    scope_value VARCHAR(80) NOT NULL,
    runmile_budget BIGINT NOT NULL CHECK (runmile_budget >= 0),
    runmile_used BIGINT NOT NULL CHECK (runmile_used >= 0),
    linked_payment_amount BIGINT NOT NULL CHECK (linked_payment_amount >= 0),
    actual_sales BIGINT NOT NULL CHECK (actual_sales >= 0),
    predicted_baseline BIGINT NOT NULL CHECK (predicted_baseline >= 0),
    estimated_incremental_sales BIGINT NOT NULL,
    effect_ratio DOUBLE PRECISION,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (scenario, scope_type, scope_value)
);

CREATE TABLE ai_model_metric (
    metric_name VARCHAR(16) PRIMARY KEY CHECK (metric_name IN ('MAE', 'MAPE', 'RMSE')),
    metric_value DOUBLE PRECISION NOT NULL CHECK (metric_value >= 0),
    evaluated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE ai_effect_evaluation (
    scenario VARCHAR(16) PRIMARY KEY CHECK (scenario IN ('NONE', 'LOW', 'MEDIUM', 'HIGH')),
    injected_effect BIGINT NOT NULL,
    estimated_effect BIGINT NOT NULL,
    difference BIGINT NOT NULL,
    difference_pct DOUBLE PRECISION,
    evaluated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_completion_runner_id ON completion(runner_id);
CREATE INDEX idx_nft_record_runner_id ON nft_record(runner_id);
CREATE INDEX idx_runmile_wallet_runner_id ON runmile_wallet(runner_id);
CREATE INDEX idx_runmile_transaction_wallet_id ON runmile_transaction(wallet_id);
CREATE INDEX idx_runmile_transaction_payment_id ON runmile_transaction(payment_id);
CREATE UNIQUE INDEX uq_runmile_transaction_wallet_issue
    ON runmile_transaction(wallet_id)
    WHERE type = 'ISSUE';
CREATE INDEX idx_payment_runner_id ON payment(runner_id);
CREATE INDEX idx_payment_merchant_id ON payment(merchant_id);
CREATE INDEX idx_merchant_daily_sales_date ON merchant_daily_sales(date);
CREATE INDEX idx_ai_prediction_date ON ai_prediction(date);
CREATE INDEX idx_policy_effect_scope ON policy_effect(scope_type, scope_value);
