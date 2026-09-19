package com.runmile.demo.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.runmile.payment.repository.PaymentRepository;
import com.runmile.runner.domain.Runner;
import com.runmile.runner.repository.RunnerRepository;
import com.runmile.wallet.domain.RunMileWallet;
import com.runmile.wallet.repository.RunMileTransactionRepository;
import com.runmile.wallet.repository.RunMileWalletRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class DemoResetServiceTests {
    private RunnerRepository runnerRepository;
    private PaymentRepository paymentRepository;
    private RunMileTransactionRepository transactionRepository;
    private RunMileWalletRepository walletRepository;
    private DemoResetService service;

    @BeforeEach
    void setUp() {
        runnerRepository = mock(RunnerRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        transactionRepository = mock(RunMileTransactionRepository.class);
        walletRepository = mock(RunMileWalletRepository.class);
        service = new DemoResetService(
                runnerRepository,
                paymentRepository,
                transactionRepository,
                walletRepository
        );
    }

    @Test
    void 데모_참가자의_지갑을_잠근_뒤_거래와_결제를_초기화한다() {
        Runner runner = mock(Runner.class);
        RunMileWallet wallet = mock(RunMileWallet.class);
        when(runner.getId()).thenReturn(17L);
        when(runnerRepository.findByRunnerCode("RUNNER_00001")).thenReturn(Optional.of(runner));
        when(walletRepository.findByRunnerIdForUpdate(17L)).thenReturn(Optional.of(wallet));

        service.reset();

        InOrder order = inOrder(
                runnerRepository,
                walletRepository,
                transactionRepository,
                paymentRepository,
                wallet
        );
        order.verify(runnerRepository).findByRunnerCode("RUNNER_00001");
        order.verify(walletRepository).findByRunnerIdForUpdate(17L);
        order.verify(transactionRepository).deleteAllByWalletRunnerId(17L);
        order.verify(paymentRepository).deleteAllByRunnerId(17L);
        order.verify(wallet).resetDemoState();
    }

    @Test
    void 데모_참가자가_없으면_데이터를_삭제하지_않는다() {
        when(runnerRepository.findByRunnerCode("RUNNER_00001")).thenReturn(Optional.empty());

        assertThatThrownBy(service::reset)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("데모 참가자를 찾을 수 없습니다.");

        verify(transactionRepository, never()).deleteAllByWalletRunnerId(17L);
        verify(paymentRepository, never()).deleteAllByRunnerId(17L);
    }
}
