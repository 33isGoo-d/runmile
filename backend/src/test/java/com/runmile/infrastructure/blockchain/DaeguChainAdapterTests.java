package com.runmile.infrastructure.blockchain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

class DaeguChainAdapterTests {
    private static final String TRUSTED = "0xe957f0ad734260f9677b4d53e8d42c0c86556367";
    private static final String TX_HASH = "0x" + "ab".repeat(32);
    private static final String INPUT = "0x" + "12".repeat(32);

    @Test
    void 실제_RPC_응답이_모두_유효하면_검증에_성공한다() throws Exception {
        CompletionAnchorPayload payload = payload();
        RpcMocks rpc = rpcMocks();
        rpc.chainId.setResult("0x13882");
        rpc.receipt.setResult(receipt("0x1", TRUSTED, TX_HASH));
        rpc.transaction.setResult(transaction(
                TRUSTED, TRUSTED, TX_HASH, "0x0", payload.toHexData()));
        rpc.blockNumber.setResult("0x64");

        BlockchainVerificationResult result = adapter(rpc.web3j)
                .verify(payload, TX_HASH, "POLYGON_AMOY");

        assertThat(result).isEqualTo(BlockchainVerificationResult.VERIFIED);
    }

    @Test
    void 같은_완주_증명은_캐시되어_RPC를_반복_호출하지_않는다() throws Exception {
        CompletionAnchorPayload payload = payload();
        RpcMocks rpc = rpcMocks();
        rpc.chainId.setResult("0x13882");
        rpc.receipt.setResult(receipt("0x1", TRUSTED, TX_HASH));
        rpc.transaction.setResult(transaction(
                TRUSTED, TRUSTED, TX_HASH, "0x0", payload.toHexData()));
        rpc.blockNumber.setResult("0x64");
        DaeguChainAdapter adapter = adapter(rpc.web3j);

        assertThat(adapter.verify(payload, TX_HASH, "POLYGON_AMOY"))
                .isEqualTo(BlockchainVerificationResult.VERIFIED);
        assertThat(adapter.verify(payload, TX_HASH, "POLYGON_AMOY"))
                .isEqualTo(BlockchainVerificationResult.VERIFIED);

        verify(rpc.web3j, times(1)).ethChainId();
    }

    @Test
    void RPC_체인_ID가_다르면_일시적_사용불가로_처리한다() throws Exception {
        RpcMocks rpc = rpcMocks();
        rpc.chainId.setResult("0x1");

        BlockchainVerificationResult result = adapter(rpc.web3j)
                .verify(payload(), TX_HASH, "POLYGON_AMOY");

        assertThat(result).isEqualTo(BlockchainVerificationResult.UNAVAILABLE);
    }

    @Test
    void RPC가_오류를_반환하면_일시적_사용불가로_처리한다() throws Exception {
        RpcMocks rpc = rpcMocks();
        rpc.chainId.setResult("0x13882");
        rpc.receipt.setError(new Response.Error(-32000, "rpc error"));

        BlockchainVerificationResult result = adapter(rpc.web3j)
                .verify(payload(), TX_HASH, "POLYGON_AMOY");

        assertThat(result).isEqualTo(BlockchainVerificationResult.UNAVAILABLE);
    }

    @Test
    void RPC_호출_예외는_일시적_사용불가로_처리한다() throws Exception {
        Web3j web3j = mock(Web3j.class);
        @SuppressWarnings("unchecked")
        Request<?, EthChainId> chainRequest = mock(Request.class);
        doReturn(chainRequest).when(web3j).ethChainId();
        when(chainRequest.send()).thenThrow(new IOException("network down"));

        BlockchainVerificationResult result = adapter(web3j)
                .verify(payload(), TX_HASH, "POLYGON_AMOY");

        assertThat(result).isEqualTo(BlockchainVerificationResult.UNAVAILABLE);
    }

    @Test
    void 성공한_자기전송이고_payload가_일치하면_유효하다() {
        assertThat(DaeguChainAdapter.isValidAnchor(
                receipt("0x1", TRUSTED, TX_HASH),
                transaction(TRUSTED, TRUSTED, TX_HASH, "0x0", INPUT),
                TRUSTED,
                TX_HASH,
                INPUT
        )).isTrue();
    }

    @Test
    void 다른_완주_payload면_무효다() {
        assertThat(DaeguChainAdapter.isValidAnchor(
                receipt("0x1", TRUSTED, TX_HASH),
                transaction(TRUSTED, TRUSTED, TX_HASH, "0x0", "0x" + "34".repeat(32)),
                TRUSTED,
                TX_HASH,
                INPUT
        )).isFalse();
    }

    @Test
    void 다른_주소로_보낸_거래면_무효다() {
        assertThat(DaeguChainAdapter.isValidAnchor(
                receipt("0x1", TRUSTED, TX_HASH),
                transaction(TRUSTED, "0x0000000000000000000000000000000000000001", TX_HASH, "0x0", INPUT),
                TRUSTED,
                TX_HASH,
                INPUT
        )).isFalse();
    }

    @Test
    void 금액을_전송한_거래면_무효다() {
        assertThat(DaeguChainAdapter.isValidAnchor(
                receipt("0x1", TRUSTED, TX_HASH),
                transaction(TRUSTED, TRUSTED, TX_HASH, "0x1", INPUT),
                TRUSTED,
                TX_HASH,
                INPUT
        )).isFalse();
    }

    @Test
    void 영수증과_거래_해시가_다르면_무효다() {
        assertThat(DaeguChainAdapter.isValidAnchor(
                receipt("0x1", TRUSTED, "0x" + "cd".repeat(32)),
                transaction(TRUSTED, TRUSTED, TX_HASH, "0x0", INPUT),
                TRUSTED,
                TX_HASH,
                INPUT
        )).isFalse();
    }

    @Test
    void 실패한_거래거나_영수증이_없으면_무효다() {
        Transaction transaction = transaction(TRUSTED, TRUSTED, TX_HASH, "0x0", INPUT);

        assertThat(DaeguChainAdapter.isValidAnchor(
                receipt("0x0", TRUSTED, TX_HASH), transaction, TRUSTED, TX_HASH, INPUT)).isFalse();
        assertThat(DaeguChainAdapter.isValidAnchor(
                null, transaction, TRUSTED, TX_HASH, INPUT)).isFalse();
    }

    @Test
    void 요청한_거래_해시와_응답이_다르면_무효다() {
        assertThat(DaeguChainAdapter.isValidAnchor(
                receipt("0x1", TRUSTED, TX_HASH),
                transaction(TRUSTED, TRUSTED, TX_HASH, "0x0", INPUT),
                TRUSTED,
                "0x" + "cd".repeat(32),
                INPUT
        )).isFalse();
    }

    @Test
    void 최소_확인_블록_수를_충족해야_한다() {
        assertThat(DaeguChainAdapter.hasRequiredConfirmations(
                java.math.BigInteger.valueOf(100),
                java.math.BigInteger.valueOf(100),
                1
        )).isTrue();
        assertThat(DaeguChainAdapter.hasRequiredConfirmations(
                java.math.BigInteger.valueOf(100),
                java.math.BigInteger.valueOf(101),
                3
        )).isFalse();
        assertThat(DaeguChainAdapter.hasRequiredConfirmations(
                java.math.BigInteger.valueOf(100),
                java.math.BigInteger.valueOf(102),
                3
        )).isTrue();
    }

    private TransactionReceipt receipt(String status, String from, String transactionHash) {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus(status);
        receipt.setFrom(from);
        receipt.setTo(TRUSTED);
        receipt.setTransactionHash(transactionHash);
        receipt.setBlockNumber("0x64");
        receipt.setBlockHash("0x" + "ef".repeat(32));
        return receipt;
    }

    private Transaction transaction(
            String from,
            String to,
            String hash,
            String value,
            String input
    ) {
        Transaction transaction = new Transaction();
        transaction.setFrom(from);
        transaction.setTo(to);
        transaction.setHash(hash);
        transaction.setValue(value);
        transaction.setInput(input);
        return transaction;
    }

    private CompletionAnchorPayload payload() {
        return new CompletionAnchorPayload(
                1L,
                "RUNNER_00001",
                1L,
                "FULL",
                12840,
                Instant.parse("2026-02-22T03:30:00Z")
        );
    }

    private DaeguChainAdapter adapter(Web3j web3j) {
        return new DaeguChainAdapter(
                web3j,
                "POLYGON_AMOY",
                80002L,
                TRUSTED,
                1,
                30,
                1000
        );
    }

    private RpcMocks rpcMocks() throws Exception {
        Web3j web3j = mock(Web3j.class);
        @SuppressWarnings("unchecked")
        Request<?, EthChainId> chainRequest = mock(Request.class);
        @SuppressWarnings("unchecked")
        Request<?, EthGetTransactionReceipt> receiptRequest = mock(Request.class);
        @SuppressWarnings("unchecked")
        Request<?, EthTransaction> transactionRequest = mock(Request.class);
        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> blockNumberRequest = mock(Request.class);

        EthChainId chainId = new EthChainId();
        EthGetTransactionReceipt receipt = new EthGetTransactionReceipt();
        EthTransaction transaction = new EthTransaction();
        EthBlockNumber blockNumber = new EthBlockNumber();

        doReturn(chainRequest).when(web3j).ethChainId();
        doReturn(receiptRequest).when(web3j).ethGetTransactionReceipt(anyString());
        doReturn(transactionRequest).when(web3j).ethGetTransactionByHash(anyString());
        doReturn(blockNumberRequest).when(web3j).ethBlockNumber();
        when(chainRequest.send()).thenReturn(chainId);
        when(receiptRequest.send()).thenReturn(receipt);
        when(transactionRequest.send()).thenReturn(transaction);
        when(blockNumberRequest.send()).thenReturn(blockNumber);

        return new RpcMocks(web3j, chainId, receipt, transaction, blockNumber);
    }

    private record RpcMocks(
            Web3j web3j,
            EthChainId chainId,
            EthGetTransactionReceipt receipt,
            EthTransaction transaction,
            EthBlockNumber blockNumber
    ) {
    }
}
