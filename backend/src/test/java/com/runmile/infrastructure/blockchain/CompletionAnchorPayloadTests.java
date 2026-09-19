package com.runmile.infrastructure.blockchain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class CompletionAnchorPayloadTests {
    @Test
    void 같은_완주_기록은_항상_같은_32바이트_payload를_만든다() {
        CompletionAnchorPayload payload = payload(1L, "RUNNER_00001");

        assertThat(payload.toHexData())
                .isEqualTo("0x22b3a5679fb023dd437cdbf73bdeea63b22ca57b1e1bf8d298a8038f98a04944")
                .matches("0x[0-9a-f]{64}");
    }

    @Test
    void 참가자나_완주_정보가_다르면_payload도_달라진다() {
        CompletionAnchorPayload first = payload(1L, "RUNNER_00001");
        CompletionAnchorPayload second = payload(2L, "RUNNER_00002");

        assertThat(first.toHexData()).isNotEqualTo(second.toHexData());
    }

    private CompletionAnchorPayload payload(Long runnerId, String runnerCode) {
        return new CompletionAnchorPayload(
                runnerId,
                runnerCode,
                runnerId,
                "FULL",
                12840,
                Instant.parse("2026-02-22T03:30:00Z")
        );
    }
}
