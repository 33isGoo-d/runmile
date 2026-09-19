-- Restore the already-mined Polygon Amoy proof only when the local demo data
-- exactly matches the payload anchored by the transaction.
UPDATE nft_record AS nft
SET nft_token_id = '0x8dcb81010b85807a05e196317ad1f9181a350984c654b3d621f3f13d3ad63781',
    network = 'POLYGON_AMOY',
    verified = true,
    issued_at = '2026-09-18T22:58:23Z'
FROM runner
JOIN completion ON completion.runner_id = runner.id
WHERE nft.runner_id = runner.id
  AND nft.completion_id = completion.id
  AND runner.id = 1
  AND completion.id = 1
  AND runner.runner_code = 'RUNNER_00001'
  AND runner.course = 'FULL'
  AND completion.completed = true
  AND completion.finish_time_seconds = 12840
  AND completion.completed_at = '2026-02-22T12:30:00+09:00';
