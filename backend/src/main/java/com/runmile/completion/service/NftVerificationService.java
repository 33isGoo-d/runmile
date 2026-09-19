package com.runmile.completion.service;

import com.runmile.completion.domain.NftRecord;
import com.runmile.completion.dto.NftVerificationResponse;
import com.runmile.completion.repository.NftRecordRepository;
import com.runmile.global.ApiException;
import com.runmile.infrastructure.blockchain.BlockchainVerificationPort;
import com.runmile.infrastructure.blockchain.BlockchainVerificationResult;
import com.runmile.infrastructure.blockchain.CompletionAnchorPayload;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NftVerificationService {
    private final NftRecordRepository nftRecordRepository;
    private final BlockchainVerificationPort blockchainVerificationPort;

    @Value("${runmile.demo-completion-proof.enabled:false}")
    private boolean demoCompletionProofEnabled;

    public NftVerificationService(
            NftRecordRepository nftRecordRepository,
            BlockchainVerificationPort blockchainVerificationPort
    ) {
        this.nftRecordRepository = nftRecordRepository;
        this.blockchainVerificationPort = blockchainVerificationPort;
    }

    public NftVerificationResponse getNftVerification(Long runnerId) {
        NftRecord nftRecord = nftRecordRepository.findByRunnerId(runnerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "NFT_NOT_FOUND",
                        "NFT 기록을 찾을 수 없습니다."
                ));

        if (!nftRecord.isVerified()) {
            return NftVerificationResponse.from(nftRecord, false);
        }

        if (demoCompletionProofEnabled && "DEMO_MOCK".equals(nftRecord.getNetwork())) {
            return NftVerificationResponse.from(nftRecord, true);
        }

        BlockchainVerificationResult result = blockchainVerificationPort.verify(
                CompletionAnchorPayload.from(nftRecord),
                nftRecord.getNftTokenId(),
                nftRecord.getNetwork()
        );
        if (result == BlockchainVerificationResult.UNAVAILABLE) {
            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "BLOCKCHAIN_UNAVAILABLE",
                    "블록체인 검증 서비스에 일시적으로 연결할 수 없습니다."
            );
        }

        return NftVerificationResponse.from(
                nftRecord,
                result == BlockchainVerificationResult.VERIFIED
        );
    }
}
