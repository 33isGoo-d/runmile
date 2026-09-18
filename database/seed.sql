INSERT INTO runner (runner_code, course)
VALUES ('RUNNER_00001', 'FULL')
ON CONFLICT (runner_code) DO NOTHING;

INSERT INTO completion (runner_id, finish_time_seconds, completed, completed_at)
SELECT id, 12840, true, '2026-02-22T12:30:00+09:00'
FROM runner
WHERE runner_code = 'RUNNER_00001'
ON CONFLICT (runner_id) DO NOTHING;

INSERT INTO nft_record (
    runner_id,
    completion_id,
    nft_token_id,
    network,
    verified,
    issued_at
)
SELECT
    runner.id,
    completion.id,
    'PENDING-RUNNER-00001',
    'PENDING',
    false,
    NULL
FROM runner
JOIN completion ON completion.runner_id = runner.id
WHERE runner.runner_code = 'RUNNER_00001'
ON CONFLICT (nft_token_id) DO NOTHING;

INSERT INTO runmile_wallet (runner_id, balance, total_issued, total_used)
SELECT id, 0, 0, 0
FROM runner
WHERE runner_code = 'RUNNER_00001'
ON CONFLICT (runner_id) DO NOTHING;

INSERT INTO merchant (merchant_code, name, district, category, address, latitude, longitude, runmile_enabled)
VALUES ('MERCHANT_00010', 'RunMile 식당', '수성구', 'RESTAURANT', '대구광역시 수성구', 35.8400000, 128.6800000, true)
ON CONFLICT (merchant_code) DO NOTHING;
