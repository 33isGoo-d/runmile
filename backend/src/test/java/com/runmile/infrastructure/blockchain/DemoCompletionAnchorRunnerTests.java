package com.runmile.infrastructure.blockchain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.runmile.completion.domain.Completion;
import com.runmile.completion.domain.NftRecord;
import com.runmile.completion.repository.NftRecordRepository;
import com.runmile.global.type.Course;
import com.runmile.runner.domain.Runner;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.response.TransactionReceiptProcessor;

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

    @Test
    void 복구_거래가_완주_증명과_일치하면_DB에_저장한다() throws Exception {
        RecoveryFixture fixture = recoveryFixture(false);

        fixture.runner.run();

        verify(fixture.record).anchorOnChain(
                RECOVERY_HASH,
                "POLYGON_AMOY",
                Instant.parse("2026-02-22T03:40:00Z")
        );
        verify(fixture.repository).saveAndFlush(fixture.record);
    }

    @Test
    void 복구_거래의_payload가_다르면_DB에_저장하지_않는다() throws Exception {
        RecoveryFixture fixture = recoveryFixture(true);

        assertThatThrownBy(fixture.runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("완주 기록과 일치하지 않는 체인 거래");

        verify(fixture.record, never()).anchorOnChain(anyString(), anyString(), any());
        verify(fixture.repository, never()).saveAndFlush(any());
    }

    private RecoveryFixture recoveryFixture(boolean invalidPayload) throws Exception {
        NftRecordRepository repository = mock(NftRecordRepository.class);
        NftRecord record = mock(NftRecord.class);
        Runner participant = mock(Runner.class);
        Completion completion = mock(Completion.class);
        Web3j web3j = mock(Web3j.class);
        TransactionReceiptProcessor receiptProcessor = mock(TransactionReceiptProcessor.class);

        when(repository.findByRunnerId(1L)).thenReturn(Optional.of(record));
        when(record.getRunner()).thenReturn(participant);
        when(record.getCompletion()).thenReturn(completion);
        when(participant.getId()).thenReturn(1L);
        when(participant.getRunnerCode()).thenReturn("RUNNER_00001");
        when(participant.getCourse()).thenReturn(Course.FULL);
        when(completion.getId()).thenReturn(1L);
        when(completion.isCompleted()).thenReturn(true);
        when(completion.getFinishTimeSeconds()).thenReturn(12840);
        when(completion.getCompletedAt()).thenReturn(Instant.parse("2026-02-22T03:30:00Z"));

        CompletionAnchorPayload payload = CompletionAnchorPayload.from(record);
        TransactionReceipt receipt = receipt();
        when(receiptProcessor.waitForTransactionReceipt(RECOVERY_HASH)).thenReturn(receipt);

        Transaction transaction = transaction(invalidPayload
                ? "0x" + "34".repeat(32)
                : payload.toHexData());
        configureRpc(web3j, transaction);

        DemoCompletionAnchorRunner runner = new DemoCompletionAnchorRunner(
                repository,
                web3j,
                receiptProcessor,
                null,
                null,
                "POLYGON_AMOY",
                80002L,
                TRUSTED,
                1,
                1L,
                false,
                RECOVERY_HASH
        );
        return new RecoveryFixture(runner, repository, record);
    }

    private void configureRpc(Web3j web3j, Transaction transaction) throws Exception {
        @SuppressWarnings("unchecked")
        Request<?, EthChainId> chainIdRequest = mock(Request.class);
        @SuppressWarnings("unchecked")
        Request<?, EthTransaction> transactionRequest = mock(Request.class);
        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> blockNumberRequest = mock(Request.class);
        @SuppressWarnings("unchecked")
        Request<?, EthBlock> blockRequest = mock(Request.class);

        EthChainId chainId = new EthChainId();
        chainId.setResult("0x13882");
        EthTransaction ethTransaction = new EthTransaction();
        ethTransaction.setResult(transaction);
        EthBlockNumber blockNumber = new EthBlockNumber();
        blockNumber.setResult("0x64");
        EthBlock blockResponse = mock(EthBlock.class);
        EthBlock.Block block = mock(EthBlock.Block.class);
        when(blockResponse.getBlock()).thenReturn(block);
        when(block.getTimestamp()).thenReturn(BigInteger.valueOf(
                Instant.parse("2026-02-22T03:40:00Z").getEpochSecond()));

        doReturn(chainIdRequest).when(web3j).ethChainId();
        doReturn(transactionRequest).when(web3j).ethGetTransactionByHash(RECOVERY_HASH);
        doReturn(blockNumberRequest).when(web3j).ethBlockNumber();
        doReturn(blockRequest).when(web3j).ethGetBlockByNumber(any(), eq(false));
        when(chainIdRequest.send()).thenReturn(chainId);
        when(transactionRequest.send()).thenReturn(ethTransaction);
        when(blockNumberRequest.send()).thenReturn(blockNumber);
        when(blockRequest.send()).thenReturn(blockResponse);
    }

    private TransactionReceipt receipt() {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus("0x1");
        receipt.setFrom(TRUSTED);
        receipt.setTo(TRUSTED);
        receipt.setTransactionHash(RECOVERY_HASH);
        receipt.setBlockNumber("0x64");
        receipt.setBlockHash("0x" + "ef".repeat(32));
        return receipt;
    }

    private Transaction transaction(String input) {
        Transaction transaction = new Transaction();
        transaction.setFrom(TRUSTED);
        transaction.setTo(TRUSTED);
        transaction.setHash(RECOVERY_HASH);
        transaction.setValue("0x0");
        transaction.setInput(input);
        return transaction;
    }

    private record RecoveryFixture(
            DemoCompletionAnchorRunner runner,
            NftRecordRepository repository,
            NftRecord record
    ) {
    }
}
