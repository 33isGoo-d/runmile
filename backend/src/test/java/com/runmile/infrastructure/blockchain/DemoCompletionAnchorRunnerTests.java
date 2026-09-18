package com.runmile.infrastructure.blockchain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.runmile.completion.repository.NftRecordRepository;
import org.junit.jupiter.api.Test;

class DemoCompletionAnchorRunnerTests {
    private static final String TRUSTED = "0xe957f0ad734260f9677b4d53e8d42c0c86556367";
    private static final String RECOVERY_HASH = "0x" + "ab".repeat(32);

    @Test
    void 복구_모드는_개인키_없이_초기화할_수_있다() {
        DemoCompletionAnchorRunner runner = new DemoCompletionAnchorRunner(
                mock(NftRecordRepository.class),
                "http://localhost:8545",
                "POLYGON_AMOY",
                80002L,
                TRUSTED,
                "",
                1,
                1,
                "1",
                false,
                RECOVERY_HASH
        );

        assertThatCode(runner::close).doesNotThrowAnyException();
    }

    @Test
    void 새_앵커링은_개인키가_필수다() {
        assertThatThrownBy(() -> new DemoCompletionAnchorRunner(
                mock(NftRecordRepository.class),
                "http://localhost:8545",
                "POLYGON_AMOY",
                80002L,
                TRUSTED,
                "",
                1,
                1,
                "1",
                false,
                ""
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DAEGU_CHAIN_PRIVATE_KEY");
    }

    @Test
    void 대상_참가자나_전체_처리_선택이_없으면_중단한다() {
        assertThatThrownBy(() -> new DemoCompletionAnchorRunner(
                mock(NftRecordRepository.class),
                "http://localhost:8545",
                "POLYGON_AMOY",
                80002L,
                TRUSTED,
                "dummy",
                1,
                1,
                "",
                false,
                ""
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DAEGU_CHAIN_ANCHOR_RUNNER_ID");
    }
}
