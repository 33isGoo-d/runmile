package com.runmile.infrastructure.blockchain;

public interface BlockchainVerificationPort {
    BlockchainVerificationResult verify(
            CompletionAnchorPayload payload,
            String transactionHash,
            String network
    );
}
