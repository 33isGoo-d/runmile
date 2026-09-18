package com.runmile.completion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.runmile.completion.domain.Completion;
import com.runmile.completion.domain.NftRecord;
import com.runmile.completion.dto.NftVerificationResponse;
import com.runmile.completion.repository.NftRecordRepository;
import com.runmile.global.ApiException;
import com.runmile.global.type.Course;
import com.runmile.infrastructure.blockchain.BlockchainVerificationPort;
import com.runmile.infrastructure.blockchain.BlockchainVerificationResult;
import com.runmile.infrastructure.blockchain.CompletionAnchorPayload;
import com.runmile.runner.domain.Runner;
import java.time.Instant;
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
    void 저장_상태와_온체인_검증이_모두_참이면_검증된_완주증명을_반환한다() {
        NftRecord nftRecord = verifiedNftRecord();
        when(nftRecordRepository.findByRunnerId(1L)).thenReturn(Optional.of(nftRecord));
        when(blockchainVerificationPort.verify(
                any(CompletionAnchorPayload.class),
                eq("0x" + "ab".repeat(32)),
                eq("POLYGON_AMOY")
        )).thenReturn(BlockchainVerificationResult.VERIFIED);

        NftVerificationResponse response = nftVerificationService.getNftVerification(1L);

        assertThat(response).isEqualTo(new NftVerificationResponse(
                "0x" + "ab".repeat(32),
                "POLYGON_AMOY",
                true
        ));
    }

    @Test
    void 온체인_검증이_거절되면_verified는_false다() {
        NftRecord nftRecord = verifiedNftRecord();
        when(nftRecordRepository.findByRunnerId(1L)).thenReturn(Optional.of(nftRecord));
        when(blockchainVerificationPort.verify(
                any(CompletionAnchorPayload.class),
                eq("0x" + "ab".repeat(32)),
                eq("POLYGON_AMOY")
        )).thenReturn(BlockchainVerificationResult.NOT_VERIFIED);

        NftVerificationResponse response = nftVerificationService.getNftVerification(1L);

        assertThat(response.verified()).isFalse();
    }

    @Test
    void 체인_RPC가_응답하지_않으면_503_오류를_반환한다() {
        NftRecord nftRecord = verifiedNftRecord();
        when(nftRecordRepository.findByRunnerId(1L)).thenReturn(Optional.of(nftRecord));
        when(blockchainVerificationPort.verify(
                any(CompletionAnchorPayload.class),
                eq("0x" + "ab".repeat(32)),
                eq("POLYGON_AMOY")
        )).thenReturn(BlockchainVerificationResult.UNAVAILABLE);

        assertThatThrownBy(() -> nftVerificationService.getNftVerification(1L))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.status().value()).isEqualTo(503);
                    assertThat(exception.code()).isEqualTo("BLOCKCHAIN_UNAVAILABLE");
                });
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
        Runner runner = mock(Runner.class);
        when(runner.getId()).thenReturn(1L);
        when(runner.getRunnerCode()).thenReturn("RUNNER_00001");
        when(runner.getCourse()).thenReturn(Course.FULL);
        Completion completion = mock(Completion.class);
        when(completion.getId()).thenReturn(1L);
        when(completion.isCompleted()).thenReturn(true);
        when(completion.getFinishTimeSeconds()).thenReturn(12840);
        when(completion.getCompletedAt()).thenReturn(Instant.parse("2026-02-22T03:30:00Z"));
        NftRecord nftRecord = mock(NftRecord.class);
        when(nftRecord.getRunner()).thenReturn(runner);
        when(nftRecord.getCompletion()).thenReturn(completion);
        when(nftRecord.getNftTokenId()).thenReturn("0x" + "ab".repeat(32));
        when(nftRecord.getNetwork()).thenReturn("POLYGON_AMOY");
        when(nftRecord.isVerified()).thenReturn(true);
        return nftRecord;
    }
}
