package com.runmile.demo.service;

import com.runmile.payment.repository.PaymentRepository;
import com.runmile.wallet.repository.RunMileTransactionRepository;
import com.runmile.wallet.repository.RunMileWalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoResetService {
    private static final long DEMO_RUNNER_ID = 1L;

    private final PaymentRepository paymentRepository;
    private final RunMileTransactionRepository transactionRepository;
    private final RunMileWalletRepository walletRepository;

    public DemoResetService(
            PaymentRepository paymentRepository,
            RunMileTransactionRepository transactionRepository,
            RunMileWalletRepository walletRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public void reset() {
        transactionRepository.deleteAllByWalletRunnerId(DEMO_RUNNER_ID);
        paymentRepository.deleteAllByRunnerId(DEMO_RUNNER_ID);
        walletRepository.findByRunnerIdForUpdate(DEMO_RUNNER_ID)
                .orElseThrow(() -> new IllegalStateException("데모 참가자 지갑을 찾을 수 없습니다."))
                .resetDemoState();
    }
}