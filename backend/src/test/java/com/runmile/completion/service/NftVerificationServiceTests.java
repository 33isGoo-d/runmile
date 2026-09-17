package com.runmile.completion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.runmile.completion.domain.NftRecord;
import com.runmile.completion.dto.NftVerificationResponse;
import com.runmile.completion.repository.NftRecordRepository;
import com.runmile.global.ApiException;
import com.runmile.infrastructure.blockchain.BlockchainVerificationPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NftVerificationServiceTests {
    private NftRecordRepository nftRecordRepository;
    private BlockchainVerificationPort blockchainVerificationPort;
    private NftVerificationService nftVerificationService;

    @BeforeEach
    void setUp() {
        nftRecordRepository = mock(NftRecordRepository.class);
        blockchainVerificationPort = mock(BlockchainVerificationPort.class);
        nftVerificationService = new NftVerificationService(
                nftRecordRepository,
                blockchainVerificationPort
        );
    }

    @Test
    void 저장_상태와_Mock_검증이_모두_참이면_검증된_NFT를_반환한다() {
        NftRecord nftRecord = verifiedNftRecord();
        when(nftRecordRepository.findByRunnerId(1L)).thenReturn(Optional.of(nftRecord));
        when(blockchainVerificationPort.isNftVerified(1L)).thenReturn(true);

        NftVerificationResponse response = nftVerificationService.getNftVerification(1L);

        assertThat(response).isEqualTo(new NftVerificationResponse(
                "DAEGU-MARATHON-2026-00001",
                "DAEGU_CHAIN_MOCK",
                true
        ));
    }

    @Test
    void Mock_검증이_거절되면_verified는_false다() {
        NftRecord nftRecord = verifiedNftRecord();
        when(nftRecordRepository.findByRunnerId(1L)).thenReturn(Optional.of(nftRecord));
        when(blockchainVerificationPort.isNftVerified(1L)).thenReturn(false);

        NftVerificationResponse response = nftVerificationService.getNftVerification(1L);

        assertThat(response.verified()).isFalse();
    }

    @Test
    void NFT_기록이_없으면_404_오류를_반환한다() {
        when(nftRecordRepository.findByRunnerId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> nftVerificationService.getNftVerification(999L))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.status().value()).isEqualTo(404);
                    assertThat(exception.code()).isEqualTo("NFT_NOT_FOUND");
                    assertThat(exception.getMessage()).isEqualTo("NFT 기록을 찾을 수 없습니다.");
                });
        verifyNoInteractions(blockchainVerificationPort);
    }

    private NftRecord verifiedNftRecord() {
        NftRecord nftRecord = mock(NftRecord.class);
        when(nftRecord.getNftTokenId()).thenReturn("DAEGU-MARATHON-2026-00001");
        when(nftRecord.getNetwork()).thenReturn("DAEGU_CHAIN_MOCK");
        when(nftRecord.isVerified()).thenReturn(true);
        return nftRecord;
    }
}
