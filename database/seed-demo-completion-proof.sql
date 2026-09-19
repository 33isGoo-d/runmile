-- Local presentation-only completion proof for RUNNER_00001.
-- This is intentionally not a Polygon Amoy transaction or an ERC-721 NFT.
UPDATE nft_record
SET nft_token_id = 'DEMO-COMPLETION-RUNNER-00001',
    network = 'DEMO_MOCK',
    verified = true,
    issued_at = now()
WHERE runner_id = 1;
