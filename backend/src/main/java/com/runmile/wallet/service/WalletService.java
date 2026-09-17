package com.runmile.wallet.service;

import com.runmile.completion.domain.Completion;
import com.runmile.completion.dto.NftVerificationResponse;
import com.runmile.completion.repository.CompletionRepository;
import com.runmile.completion.service.NftVerificationService;
import com.runmile.global.ApiException;
import com.runmile.global.type.RunMileTransactionType;
import com.runmile.wallet.domain.RunMileTransaction;
import com.runmile.wallet.domain.RunMileWallet;
import com.runmile.wallet.dto.RunMileIssueResponse;
import com.runmile.wallet.dto.RunMileTransactionResponse;
import com.runmile.wallet.dto.WalletResponse;
import com.runmile.wallet.repository.RunMileTransactionRepository;
import com.runmile.wallet.repository.RunMileWalletRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {
    private static final long COMPLETION_REWARD = 10_000L;

    private final RunMileWalletRepository walletRepository;
    private final RunMileTransactionRepository transactionRepository;
    private final CompletionRepository completionRepository;
    private final NftVerificationService nftVerificationService;

    public WalletService(
            RunMileWalletRepository walletRepository,
            RunMileTransactionRepository transactionRepository,
            CompletionRepository completionRepository,
            NftVerificationService nftVerificationService
    ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.completionRepository = completionRepository;
        this.nftVerificationService = nftVerificationService;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long runnerId) {
        return WalletResponse.from(findWallet(runnerId));
    }

    @Transactional(readOnly = true)
    public List<RunMileTransactionResponse> getTransactions(Long runnerId) {
        findWallet(runnerId);
        return transactionRepository.findAllByWalletRunnerIdOrderByCreatedAtDesc(runnerId).stream()
                .map(RunMileTransactionResponse::from)
                .toList();
    }

    @Transactional
    public RunMileIssueResponse issue(Long runnerId, long amount) {
        if (amount != COMPLETION_REWARD) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_RUNMILE_AMOUNT",
                    "완주 보상은 10,000 RunMile만 지급할 수 있습니다."
            );
        }

        Completion completion = completionRepository.findByRunnerId(runnerId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "COMPLETION_NOT_FOUND",
                        "완주 기록을 찾을 수 없습니다."
                ));
        if (!completion.isCompleted()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "COMPLETION_NOT_VERIFIED",
                    "완주가 확인되지 않았습니다."
            );
        }

        NftVerificationResponse nft = nftVerificationService.getNftVerification(runnerId);
        if (!nft.verified()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "NFT_NOT_VERIFIED",
                    "NFT 검증이 완료되지 않았습니다."
            );
        }

        RunMileWallet wallet = walletRepository.findByRunnerIdForUpdate(runnerId)
                .orElseThrow(this::walletNotFound);
        if (transactionRepository.existsByWalletIdAndType(
                wallet.getId(),
                RunMileTransactionType.ISSUE
        )) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "RUNMILE_ALREADY_ISSUED",
                    "이미 완주 RunMile이 지급되었습니다."
            );
        }

        wallet.issue(amount);
        transactionRepository.save(RunMileTransaction.issue(wallet, amount));
        return new RunMileIssueResponse(amount, wallet.getBalance());
    }

    private RunMileWallet findWallet(Long runnerId) {
        return walletRepository.findByRunnerId(runnerId)
                .orElseThrow(this::walletNotFound);
    }

    private ApiException walletNotFound() {
        return new ApiException(
                HttpStatus.NOT_FOUND,
                "WALLET_NOT_FOUND",
                "RunMile 지갑을 찾을 수 없습니다."
        );
    }
}
