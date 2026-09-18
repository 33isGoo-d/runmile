UPDATE nft_record
SET nft_token_id = 'PENDING-RUNNER-' || LPAD(runner_id::text, 5, '0'),
    network = 'PENDING',
    verified = false,
    issued_at = NULL
WHERE network = 'DAEGU_CHAIN_MOCK';
