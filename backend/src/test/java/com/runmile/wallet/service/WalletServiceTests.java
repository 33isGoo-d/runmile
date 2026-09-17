package com.runmile.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.runmile.completion.domain.Completion;
import com.runmile.completion.dto.NftVerificationResponse;
import com.runmile.completion.repository.CompletionRepository;
import com.runmile.completion.service.NftVerificationService;
import com.runmile.global.ApiException;
import com.runmile.global.type.RunMileTransactionType;
import com.runmile.wallet.domain.RunMileTransaction;
import com.runmile.wallet.domain.RunMileWallet;
import com.runmile.wallet.dto.RunMileIssueResponse;
import com.runmile.wallet.repository.RunMileTransactionRepository;
import com.runmile.wallet.repository.RunMileWalletRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WalletServiceTests {
    private RunMileWalletRepository walletRepository;
    private RunMileTransactionRepository transactionRepository;
    private CompletionRepository completionRepository;
    private NftVerificationService nftVerificationService;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletRepository = mock(RunMileWalletRepository.class);
        transactionRepository = mock(RunMileTransactionRepository.class);
        completionRepository = mock(CompletionRepository.class);
        nftVerificationService = mock(NftVerificationService.class);
        walletService = new WalletService(
                walletRepository,
                transactionRepository,
                completionRepository,
                nftVerificationService
        );
    }

    @Test
    void 완주와_NFT를_확인하고_RunMile을_지급한다() {
        Completion completion = mock(Completion.class);
        RunMileWallet wallet = mock(RunMileWallet.class);
        when(completion.isCompleted()).thenReturn(true);
        when(completionRepository.findByRunnerId(1L)).thenReturn(Optional.of(completion));
        when(nftVerificationService.getNftVerification(1L))
                .thenReturn(new NftVerificationResponse("TOKEN", "DAEGU_CHAIN_MOCK", true));
        when(walletRepository.findByRunnerIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(wallet.getId()).thenReturn(1L);
        when(wallet.getBalance()).thenReturn(10_000L);
        when(transactionRepository.existsByWalletIdAndType(1L, RunMileTransactionType.ISSUE))
                .thenReturn(false);

        RunMileIssueResponse response = walletService.issue(1L, 10_000L);

        assertThat(response).isEqualTo(new RunMileIssueResponse(10_000L, 10_000L));
        verify(wallet).issue(10_000L);
        verify(transactionRepository).save(any(RunMileTransaction.class));
    }

    @Test
    void 이미_지급된_지갑은_중복_지급하지_않는다() {
        Completion completion = mock(Completion.class);
        RunMileWallet wallet = mock(RunMileWallet.class);
        when(completion.isCompleted()).thenReturn(true);
        when(completionRepository.findByRunnerId(1L)).thenReturn(Optional.of(completion));
        when(nftVerificationService.getNftVerification(1L))
                .thenReturn(new NftVerificationResponse("TOKEN", "DAEGU_CHAIN_MOCK", true));
        when(walletRepository.findByRunnerIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(wallet.getId()).thenReturn(1L);
        when(transactionRepository.existsByWalletIdAndType(1L, RunMileTransactionType.ISSUE))
                .thenReturn(true);

        assertThatThrownBy(() -> walletService.issue(1L, 10_000L))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.code()).isEqualTo("RUNMILE_ALREADY_ISSUED")
                );
    }
}
