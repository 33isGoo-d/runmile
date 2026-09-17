INSERT INTO runner (id, runner_code, course)
VALUES (1, 'RUNNER_00001', 'FULL')
ON CONFLICT (runner_code) DO NOTHING;

INSERT INTO completion (id, runner_id, finish_time_seconds, completed, completed_at)
VALUES (1, 1, 12840, true, '2026-02-22T12:30:00+09:00')
ON CONFLICT (runner_id) DO NOTHING;

INSERT INTO nft_record (id, runner_id, completion_id, nft_token_id, network, verified, issued_at)
VALUES (1, 1, 1, 'DAEGU-MARATHON-2026-00001', 'DAEGU_CHAIN_MOCK', true, '2026-02-22T12:35:00+09:00')
ON CONFLICT (nft_token_id) DO NOTHING;

INSERT INTO runmile_wallet (id, runner_id, balance, total_issued, total_used)
VALUES (1, 1, 0, 0, 0)
ON CONFLICT (runner_id) DO NOTHING;

INSERT INTO merchant (id, merchant_code, name, district, category, address, latitude, longitude, runmile_enabled)
VALUES (10, 'MERCHANT_00010', 'RunMile 식당', '수성구', 'RESTAURANT', '대구광역시 수성구', 35.8400000, 128.6800000, true)
ON CONFLICT DO NOTHING;
