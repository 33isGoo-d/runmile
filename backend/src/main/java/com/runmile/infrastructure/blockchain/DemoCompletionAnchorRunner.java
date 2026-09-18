package com.runmile.infrastructure.blockchain;

import com.runmile.completion.domain.NftRecord;
import com.runmile.completion.repository.NftRecordRepository;
import jakarta.annotation.PreDestroy;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

// 시드 완주 기록을 실제 테스트넷에 앵커링하는 로컬 운영 명령이다.
@Component
@Profile("anchor-demo")
public class DemoCompletionAnchorRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoCompletionAnchorRunner.class);
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(60_000);

    private final NftRecordRepository nftRecordRepository;
    private final Web3j web3j;
    private final Credentials credentials;
    private final RawTransactionManager transactionManager;
    private final String networkName;
    private final long chainId;
    private final String trustedAddress;
    private final int minimumConfirmations;
    private final Long anchorRunnerId;
    private final boolean anchorAll;
    private final String recoveryTransactionHash;

    public DemoCompletionAnchorRunner(
            NftRecordRepository nftRecordRepository,
            @Value("${daegu-chain.rpc-url}") String rpcUrl,
            @Value("${daegu-chain.network-name}") String networkName,
            @Value("${daegu-chain.chain-id}") long chainId,
            @Value("${daegu-chain.trusted-address}") String trustedAddress,
            @Value("${daegu-chain.private-key}") String privateKey,
            @Value("${daegu-chain.minimum-confirmations}") int minimumConfirmations,
            @Value("${daegu-chain.rpc-timeout-seconds}") long rpcTimeoutSeconds,
            @Value("${daegu-chain.anchor-runner-id:}") String anchorRunnerId,
            @Value("${daegu-chain.anchor-all:false}") boolean anchorAll,
            @Value("${daegu-chain.recovery-transaction-hash:}") String recoveryTransactionHash
    ) {
        if (!WalletUtils.isValidAddress(trustedAddress)) {
            throw new IllegalArgumentException("DAEGU_CHAIN_TRUSTED_ADDRESS가 올바른 지갑 주소가 아닙니다.");
        }
        this.nftRecordRepository = nftRecordRepository;
        if (minimumConfirmations <= 0) {
            throw new IllegalArgumentException("DAEGU_CHAIN_MINIMUM_CONFIRMATIONS는 1 이상이어야 합니다.");
        }
        Long parsedRunnerId = parseRunnerId(anchorRunnerId);
        String normalizedRecoveryHash = recoveryTransactionHash == null
                ? ""
                : recoveryTransactionHash.trim();
        if (!normalizedRecoveryHash.isBlank()
                && !normalizedRecoveryHash.matches("0x[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("DAEGU_CHAIN_RECOVERY_TX_HASH 형식이 올바르지 않습니다.");
        }
        if (!normalizedRecoveryHash.isBlank() && parsedRunnerId == null) {
            throw new IllegalArgumentException("복구할 때는 DAEGU_CHAIN_ANCHOR_RUNNER_ID가 필요합니다.");
        }
        if (!normalizedRecoveryHash.isBlank() && anchorAll) {
            throw new IllegalArgumentException("복구 모드와 DAEGU_CHAIN_ANCHOR_ALL은 함께 사용할 수 없습니다.");
        }
        if (normalizedRecoveryHash.isBlank() && parsedRunnerId == null && !anchorAll) {
            throw new IllegalArgumentException(
                    "DAEGU_CHAIN_ANCHOR_RUNNER_ID를 지정하거나 "
                            + "DAEGU_CHAIN_ANCHOR_ALL=true를 명시해야 합니다.");
        }
        if (parsedRunnerId != null && anchorAll) {
            throw new IllegalArgumentException(
                    "DAEGU_CHAIN_ANCHOR_RUNNER_ID와 DAEGU_CHAIN_ANCHOR_ALL은 함께 사용할 수 없습니다.");
        }

        Credentials signingCredentials = null;
        if (normalizedRecoveryHash.isBlank()) {
            if (privateKey == null || privateKey.isBlank()) {
                throw new IllegalArgumentException("앵커링에는 DAEGU_CHAIN_PRIVATE_KEY가 필요합니다.");
            }
            signingCredentials = Credentials.create(privateKey);
            if (!signingCredentials.getAddress().equalsIgnoreCase(trustedAddress)) {
                throw new IllegalArgumentException("개인키와 DAEGU_CHAIN_TRUSTED_ADDRESS가 일치하지 않습니다.");
            }
        }

        this.web3j = BlockchainWeb3jFactory.create(rpcUrl, rpcTimeoutSeconds);
        this.networkName = networkName;
        this.chainId = chainId;
        this.trustedAddress = trustedAddress;
        this.minimumConfirmations = minimumConfirmations;
        this.anchorRunnerId = parsedRunnerId;
        this.anchorAll = anchorAll;
        this.recoveryTransactionHash = normalizedRecoveryHash;
        this.credentials = signingCredentials;
        this.transactionManager = signingCredentials == null
                ? null
                : new RawTransactionManager(web3j, signingCredentials, chainId);
    }

    @Override
    public void run(String... args) throws Exception {
        var chainIdResponse = web3j.ethChainId().send();
        if (chainIdResponse.hasError()) {
            throw new IllegalStateException("RPC 체인 ID 조회 실패: " + chainIdResponse.getError().getMessage());
        }
        BigInteger actualChainId = chainIdResponse.getChainId();
        if (!BigInteger.valueOf(chainId).equals(actualChainId)) {
            throw new IllegalStateException(
                    "RPC 체인 ID가 설정과 다릅니다. expected=" + chainId + ", actual=" + actualChainId);
        }
        if (!recoveryTransactionHash.isBlank()) {
            recover(findRecord(anchorRunnerId), recoveryTransactionHash);
            return;
        }

        List<NftRecord> records = anchorAll
                ? nftRecordRepository.findAll()
                : List.of(findRecord(anchorRunnerId));
        for (NftRecord record : records) {
            if (!record.getCompletion().isCompleted()) {
                if (anchorAll) {
                    log.warn("미완주 기록 건너뜀: runnerId={}, completionId={}",
                            record.getRunner().getId(), record.getCompletion().getId());
                    continue;
                }
                throw new IllegalArgumentException(
                        "완료되지 않은 기록은 체인에 앵커링할 수 없습니다. runnerId="
                                + record.getRunner().getId());
            }
            if (networkName.equals(record.getNetwork())) {
                verifyAndSave(record, record.getNftTokenId(), false);
                log.info("기존 {} 앵커 검증 완료: {}", networkName, record.getNftTokenId());
                continue;
            }
            anchor(record);
        }
    }

    private void anchor(NftRecord record) throws Exception {
        if (credentials == null || transactionManager == null) {
            throw new IllegalStateException("앵커링 서명 정보가 초기화되지 않았습니다.");
        }
        String runnerCode = record.getRunner().getRunnerCode();
        String data = CompletionAnchorPayload.from(record).toHexData();

        var gasPriceResponse = web3j.ethGasPrice().send();
        if (gasPriceResponse.hasError()) {
            throw new IllegalStateException("가스 가격 조회 실패: " + gasPriceResponse.getError().getMessage());
        }
        BigInteger gasPrice = gasPriceResponse.getGasPrice();
        var balanceResponse = web3j.ethGetBalance(
                credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        if (balanceResponse.hasError()) {
            throw new IllegalStateException("지갑 잔액 조회 실패: " + balanceResponse.getError().getMessage());
        }
        BigInteger balance = balanceResponse.getBalance();
        BigInteger maximumFee = gasPrice.multiply(GAS_LIMIT);
        if (balance.compareTo(maximumFee) < 0) {
            throw new IllegalStateException(
                    "앵커 지갑의 Amoy POL 잔액이 부족합니다. address=" + credentials.getAddress());
        }

        EthSendTransaction response = transactionManager.sendTransaction(
                gasPrice, GAS_LIMIT, trustedAddress, data, BigInteger.ZERO);
        if (response.hasError()) {
            throw new IllegalStateException("체인 앵커링 전송 실패: " + response.getError().getMessage());
        }
        String txHash = response.getTransactionHash();
        if (txHash == null || !txHash.matches("0x[0-9a-fA-F]{64}")) {
            throw new IllegalStateException("체인에서 올바른 트랜잭션 해시를 받지 못했습니다.");
        }

        log.info("체인 앵커링 전송 완료: {} -> {}", runnerCode, txHash);
        verifyAndSave(record, txHash, true);
    }

    private void recover(NftRecord record, String transactionHash) throws Exception {
        verifyAndSave(record, transactionHash, true);
        log.info("기존 온체인 거래 복구 완료: runnerId={}, txHash={}",
                record.getRunner().getId(), transactionHash);
    }

    private void verifyAndSave(
            NftRecord record,
            String transactionHash,
            boolean saveToDatabase
    ) throws Exception {
        TransactionReceipt receipt = new PollingTransactionReceiptProcessor(web3j, 4000, 40)
                .waitForTransactionReceipt(transactionHash);
        var transactionResponse = web3j.ethGetTransactionByHash(transactionHash).send();
        if (transactionResponse.hasError()) {
            throw new IllegalStateException(
                    "체인 거래 조회 실패: " + transactionResponse.getError().getMessage());
        }
        Transaction transaction = transactionResponse.getTransaction()
                .orElseThrow(() -> new IllegalStateException("체인 거래를 찾을 수 없습니다: " + transactionHash));
        String expectedInput = CompletionAnchorPayload.from(record).toHexData();
        if (!DaeguChainAdapter.isValidAnchor(
                receipt,
                transaction,
                trustedAddress,
                transactionHash,
                expectedInput
        )) {
            throw new IllegalStateException("완주 기록과 일치하지 않는 체인 거래입니다: " + transactionHash);
        }

        awaitConfirmations(receipt);
        if (!saveToDatabase) {
            return;
        }

        Instant anchoredAt = loadBlockTimestamp(receipt);
        log.info("체인 앵커링 검증 완료, DB 저장 시작: runnerId={}, txHash={}",
                record.getRunner().getId(), transactionHash);
        record.anchorOnChain(transactionHash, networkName, anchoredAt);
        nftRecordRepository.saveAndFlush(record);
        log.info("앵커링 완료: {} -> {} ({})",
                record.getRunner().getRunnerCode(), transactionHash, networkName);
    }

    private void awaitConfirmations(TransactionReceipt receipt) throws Exception {
        for (int attempt = 0; attempt < 40; attempt++) {
            var blockNumberResponse = web3j.ethBlockNumber().send();
            if (blockNumberResponse.hasError()) {
                throw new IllegalStateException(
                        "최신 블록 조회 실패: " + blockNumberResponse.getError().getMessage());
            }
            if (DaeguChainAdapter.hasRequiredConfirmations(
                    receipt.getBlockNumber(),
                    blockNumberResponse.getBlockNumber(),
                    minimumConfirmations
            )) {
                return;
            }
            Thread.sleep(4000);
        }
        throw new IllegalStateException("필요한 확인 블록 수에 도달하지 못했습니다.");
    }

    private Instant loadBlockTimestamp(TransactionReceipt receipt) throws Exception {
        var blockResponse = web3j.ethGetBlockByNumber(
                org.web3j.protocol.core.DefaultBlockParameter.valueOf(receipt.getBlockNumber()),
                false
        ).send();
        if (blockResponse.hasError() || blockResponse.getBlock() == null) {
            throw new IllegalStateException("앵커링 블록 시각을 조회하지 못했습니다.");
        }
        return Instant.ofEpochSecond(blockResponse.getBlock().getTimestamp().longValueExact());
    }

    private NftRecord findRecord(Long runnerId) {
        return nftRecordRepository.findByRunnerId(runnerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "완주 증명 기록을 찾을 수 없습니다. runnerId=" + runnerId));
    }

    private Long parseRunnerId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "DAEGU_CHAIN_ANCHOR_RUNNER_ID는 양의 정수여야 합니다.", exception);
        }
    }

    @PreDestroy
    public void close() {
        web3j.shutdown();
    }
}
