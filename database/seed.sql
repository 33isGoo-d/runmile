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
    '0x8dcb81010b85807a05e196317ad1f9181a350984c654b3d621f3f13d3ad63781',
    'POLYGON_AMOY',
    true,
    '2026-09-18T22:58:23Z'
FROM runner
JOIN completion ON completion.runner_id = runner.id
WHERE runner.runner_code = 'RUNNER_00001'
  AND runner.id = 1
  AND completion.id = 1
  AND runner.course = 'FULL'
  AND completion.completed = true
  AND completion.finish_time_seconds = 12840
  AND completion.completed_at = '2026-02-22T12:30:00+09:00'
ON CONFLICT (runner_id) DO UPDATE SET
    completion_id = EXCLUDED.completion_id,
    nft_token_id = EXCLUDED.nft_token_id,
    network = EXCLUDED.network,
    verified = EXCLUDED.verified,
    issued_at = EXCLUDED.issued_at;

INSERT INTO runmile_wallet (runner_id, balance, total_issued, total_used)
SELECT id, 0, 0, 0
FROM runner
WHERE runner_code = 'RUNNER_00001'
ON CONFLICT (runner_id) DO NOTHING;

INSERT INTO merchant (merchant_code, name, district, category, address, latitude, longitude, runmile_enabled)
VALUES ('MERCHANT_00010', 'RunMile 식당', '수성구', 'RESTAURANT', '대구광역시 수성구', 35.8400000, 128.6800000, true)
ON CONFLICT (merchant_code) DO NOTHING;
