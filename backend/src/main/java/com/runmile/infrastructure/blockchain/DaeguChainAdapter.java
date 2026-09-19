package com.runmile.infrastructure.blockchain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PreDestroy;
import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

@Component
public class DaeguChainAdapter implements BlockchainVerificationPort {
    private static final Logger log = LoggerFactory.getLogger(DaeguChainAdapter.class);

    private final Web3j web3j;
    private final String networkName;
    private final long chainId;
    private final String trustedAddress;
    private final int minimumConfirmations;
    private final Cache<String, BlockchainVerificationResult> verificationCache;

    @Autowired
    public DaeguChainAdapter(
            @Value("${daegu-chain.rpc-url}") String rpcUrl,
            @Value("${daegu-chain.network-name}") String networkName,
            @Value("${daegu-chain.chain-id}") long chainId,
            @Value("${daegu-chain.trusted-address}") String trustedAddress,
            @Value("${daegu-chain.minimum-confirmations}") int minimumConfirmations,
            @Value("${daegu-chain.rpc-timeout-seconds}") long rpcTimeoutSeconds,
            @Value("${daegu-chain.verification-cache-seconds}") long cacheSeconds,
            @Value("${daegu-chain.verification-cache-max-size}") long cacheMaxSize
    ) {
        this(
                BlockchainWeb3jFactory.create(rpcUrl, rpcTimeoutSeconds),
                networkName,
                chainId,
                trustedAddress,
                minimumConfirmations,
                cacheSeconds,
                cacheMaxSize
        );
    }

    DaeguChainAdapter(
            Web3j web3j,
            String networkName,
            long chainId,
            String trustedAddress,
            int minimumConfirmations,
            long cacheSeconds,
            long cacheMaxSize
    ) {
        if (!WalletUtils.isValidAddress(trustedAddress)) {
            throw new IllegalArgumentException("DAEGU_CHAIN_TRUSTED_ADDRESS가 올바른 지갑 주소가 아닙니다.");
        }
        if (minimumConfirmations <= 0) {
            throw new IllegalArgumentException("DAEGU_CHAIN_MINIMUM_CONFIRMATIONS는 1 이상이어야 합니다.");
        }
        if (cacheSeconds <= 0 || cacheMaxSize <= 0) {
            throw new IllegalArgumentException("블록체인 검증 캐시 설정은 1 이상이어야 합니다.");
        }
        this.web3j = web3j;
        this.networkName = networkName;
        this.chainId = chainId;
        this.trustedAddress = trustedAddress;
        this.minimumConfirmations = minimumConfirmations;
        this.verificationCache = Caffeine.newBuilder()
                .maximumSize(cacheMaxSize)
                .expireAfterWrite(Duration.ofSeconds(cacheSeconds))
                .build();
    }

    @Override
    public BlockchainVerificationResult verify(
            CompletionAnchorPayload payload,
            String transactionHash,
            String network
    ) {
        if (payload == null || !isTransactionHash(transactionHash) || !networkName.equals(network)) {
            return BlockchainVerificationResult.NOT_VERIFIED;
        }
        String cacheKey = network + ":" + transactionHash.toLowerCase() + ":" + payload.toHexData();
        AtomicReference<BlockchainVerificationResult> loaded = new AtomicReference<>();
        BlockchainVerificationResult result = verificationCache.asMap().compute(cacheKey, (key, cached) -> {
            if (cached != null) {
                return cached;
            }
            BlockchainVerificationResult verified = verifyOnChain(payload, transactionHash);
            loaded.set(verified);
            return verified == BlockchainVerificationResult.UNAVAILABLE ? null : verified;
        });
        if (result != null) {
            return result;
        }
        return loaded.get();
    }

    private BlockchainVerificationResult verifyOnChain(
            CompletionAnchorPayload payload,
            String transactionHash
    ) {
        try {
            var chainIdResponse = web3j.ethChainId().send();
            if (chainIdResponse.hasError()) {
                log.warn("체인 ID 조회 RPC 오류");
                return BlockchainVerificationResult.UNAVAILABLE;
            }
            BigInteger actualChainId = chainIdResponse.getChainId();
            if (!BigInteger.valueOf(chainId).equals(actualChainId)) {
                log.error("체인 ID 불일치: expected={}, actual={}", chainId, actualChainId);
                return BlockchainVerificationResult.UNAVAILABLE;
            }

            var receiptResponse = web3j.ethGetTransactionReceipt(transactionHash).send();
            var transactionResponse = web3j.ethGetTransactionByHash(transactionHash).send();
            if (receiptResponse.hasError() || transactionResponse.hasError()) {
                log.warn("체인 검증 RPC 오류: txHash={}", transactionHash);
                return BlockchainVerificationResult.UNAVAILABLE;
            }

            TransactionReceipt receipt = receiptResponse.getTransactionReceipt().orElse(null);
            Transaction transaction = transactionResponse.getTransaction().orElse(null);
            if (!isValidAnchor(
                    receipt,
                    transaction,
                    trustedAddress,
                    transactionHash,
                    payload.toHexData()
            )) {
                return BlockchainVerificationResult.NOT_VERIFIED;
            }

            var blockNumberResponse = web3j.ethBlockNumber().send();
            if (blockNumberResponse.hasError()) {
                log.warn("최신 블록 조회 RPC 오류: txHash={}", transactionHash);
                return BlockchainVerificationResult.UNAVAILABLE;
            }
            return hasRequiredConfirmations(
                    receipt.getBlockNumber(),
                    blockNumberResponse.getBlockNumber(),
                    minimumConfirmations
            ) ? BlockchainVerificationResult.VERIFIED : BlockchainVerificationResult.NOT_VERIFIED;
        } catch (Exception exception) {
            log.warn("체인 검증 조회 실패: txHash={}", transactionHash, exception);
            return BlockchainVerificationResult.UNAVAILABLE;
        }
    }

    static boolean isValidAnchor(
            TransactionReceipt receipt,
            Transaction transaction,
            String trustedAddress,
            String expectedTransactionHash,
            String expectedInput
    ) {
        return receipt != null
                && transaction != null
                && receipt.isStatusOK()
                && receipt.getTransactionHash() != null
                && receipt.getBlockNumberRaw() != null
                && receipt.getBlockHash() != null
                && !receipt.getBlockHash().isBlank()
                && receipt.getTransactionHash().equalsIgnoreCase(expectedTransactionHash)
                && receipt.getTransactionHash().equalsIgnoreCase(transaction.getHash())
                && sameAddress(receipt.getFrom(), trustedAddress)
                && sameAddress(receipt.getTo(), trustedAddress)
                && sameAddress(transaction.getFrom(), trustedAddress)
                && sameAddress(transaction.getTo(), trustedAddress)
                && BigInteger.ZERO.equals(transaction.getValue())
                && transaction.getInput() != null
                && transaction.getInput().equalsIgnoreCase(expectedInput);
    }

    static boolean hasRequiredConfirmations(
            BigInteger transactionBlockNumber,
            BigInteger latestBlockNumber,
            int minimumConfirmations
    ) {
        if (transactionBlockNumber == null || latestBlockNumber == null || minimumConfirmations <= 0) {
            return false;
        }
        BigInteger confirmations = latestBlockNumber
                .subtract(transactionBlockNumber)
                .add(BigInteger.ONE);
        return confirmations.compareTo(BigInteger.valueOf(minimumConfirmations)) >= 0;
    }

    private static boolean sameAddress(String actual, String expected) {
        return actual != null && actual.equalsIgnoreCase(expected);
    }

    private static boolean isTransactionHash(String value) {
        return value != null && value.matches("0x[0-9a-fA-F]{64}");
    }

    @PreDestroy
    public void close() {
        web3j.shutdown();
    }
}
