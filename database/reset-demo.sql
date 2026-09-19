BEGIN;

DO $$
DECLARE
    target_runner_id BIGINT;
    target_wallet_id BIGINT;
BEGIN
    SELECT id
    INTO target_runner_id
    FROM runner
    WHERE runner_code = 'RUNNER_00001';

    IF target_runner_id IS NULL THEN
        RAISE EXCEPTION '데모 참가자 RUNNER_00001을 찾을 수 없습니다.';
    END IF;

    SELECT id
    INTO target_wallet_id
    FROM runmile_wallet
    WHERE runner_id = target_runner_id;

    IF target_wallet_id IS NULL THEN
        RAISE EXCEPTION '데모 참가자의 RunMile 지갑을 찾을 수 없습니다.';
    END IF;

    DELETE FROM runmile_transaction
    WHERE wallet_id = target_wallet_id;

    DELETE FROM payment
    WHERE runner_id = target_runner_id;

    UPDATE runmile_wallet
    SET balance = 0,
        total_issued = 0,
        total_used = 0,
        updated_at = now()
    WHERE id = target_wallet_id;
END $$;

COMMIT;
