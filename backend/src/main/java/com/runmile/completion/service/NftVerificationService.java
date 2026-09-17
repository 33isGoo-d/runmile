package com.runmile.completion.service;

import com.runmile.completion.domain.NftRecord;
import com.runmile.completion.dto.NftVerificationResponse;
import com.runmile.completion.repository.NftRecordRepository;
import com.runmile.global.ApiException;
import com.runmile.infrastructure.blockchain.BlockchainVerificationPort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NftVerificationService {
    private final NftRecordRepository nftRecordRepository;
    private final BlockchainVerificationPort blockchainVerificationPort;

    public NftVerificationService(
            NftRecordRepository nftRecordRepository,
            BlockchainVerificationPort blockchainVerificationPort
    ) {
        this.nftRecordRepository = nftRecordRepository;
        this.blockchainVerificationPort = blockchainVerificationPort;
    }

    @Transactional(readOnly = true)
    public NftVerificationResponse getNftVerification(Long runnerId) {
        NftRecord nftRecord = nftRecordRepository.findByRunnerId(runnerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "NFT_NOT_FOUND",
                        "NFT 기록을 찾을 수 없습니다."
                ));

        boolean verified = nftRecord.isVerified()
                && blockchainVerificationPort.isNftVerified(runnerId);

        return NftVerificationResponse.from(nftRecord, verified);
    }
}
