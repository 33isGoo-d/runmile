package com.runmile.demo.service;

import com.runmile.payment.repository.PaymentRepository;
import com.runmile.runner.domain.Runner;
import com.runmile.runner.repository.RunnerRepository;
import com.runmile.wallet.domain.RunMileWallet;
import com.runmile.wallet.repository.RunMileTransactionRepository;
import com.runmile.wallet.repository.RunMileWalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoResetService {
    private static final String DEMO_RUNNER_CODE = "RUNNER_00001";

    private final RunnerRepository runnerRepository;
    private final PaymentRepository paymentRepository;
    private final RunMileTransactionRepository transactionRepository;
    private final RunMileWalletRepository walletRepository;

    public DemoResetService(
            RunnerRepository runnerRepository,
            PaymentRepository paymentRepository,
            RunMileTransactionRepository transactionRepository,
            RunMileWalletRepository walletRepository
    ) {
        this.runnerRepository = runnerRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public void reset() {
        Runner runner = runnerRepository.findByRunnerCode(DEMO_RUNNER_CODE)
                .orElseThrow(() -> new IllegalStateException("데모 참가자를 찾을 수 없습니다."));
        Long runnerId = runner.getId();
        RunMileWallet wallet = walletRepository.findByRunnerIdForUpdate(runnerId)
                .orElseThrow(() -> new IllegalStateException("데모 참가자 지갑을 찾을 수 없습니다."));

        transactionRepository.deleteAllByWalletRunnerId(runnerId);
        paymentRepository.deleteAllByRunnerId(runnerId);
        wallet.resetDemoState();
    }
}
