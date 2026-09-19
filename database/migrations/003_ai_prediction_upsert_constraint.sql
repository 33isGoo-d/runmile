-- Existing Docker volumes created before ai_prediction gained its UPSERT key
-- need this index before the AI batch can be safely rerun.
CREATE UNIQUE INDEX IF NOT EXISTS ux_ai_prediction_merchant_date_scenario
    ON ai_prediction (merchant_id, date, scenario);
