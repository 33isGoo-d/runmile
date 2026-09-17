package com.runmile.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.runmile.global.ApiException;
import com.runmile.global.type.PaymentStatus;
import com.runmile.infrastructure.payment.PaymentApproval;
import com.runmile.infrastructure.payment.PaymentGatewayPort;
import com.runmile.merchant.domain.Merchant;
import com.runmile.merchant.repository.MerchantRepository;
import com.runmile.payment.domain.Payment;
import com.runmile.payment.dto.PaymentRequest;
import com.runmile.payment.dto.PaymentResponse;
import com.runmile.payment.repository.PaymentRepository;
import com.runmile.runner.domain.Runner;
import com.runmile.runner.repository.RunnerRepository;
import com.runmile.wallet.domain.RunMileTransaction;
import com.runmile.wallet.domain.RunMileWallet;
import com.runmile.wallet.repository.RunMileTransactionRepository;
import com.runmile.wallet.repository.RunMileWalletRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentServiceTests {
    private RunnerRepository runnerRepository;
    private MerchantRepository merchantRepository;
    private RunMileWalletRepository walletRepository;
    private PaymentRepository paymentRepository;
    private RunMileTransactionRepository transactionRepository;
    private PaymentGatewayPort paymentGatewayPort;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        runnerRepository = mock(RunnerRepository.class);
        merchantRepository = mock(MerchantRepository.class);
        walletRepository = mock(RunMileWalletRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        transactionRepository = mock(RunMileTransactionRepository.class);
        paymentGatewayPort = mock(PaymentGatewayPort.class);
        paymentService = new PaymentService(
                runnerRepository,
                merchantRepository,
                walletRepository,
                paymentRepository,
                transactionRepository,
                paymentGatewayPort
        );
    }

    @Test
    void 복합_결제를_저장하고_RunMile을_차감한다() {
        Runner runner = mock(Runner.class);
        Merchant merchant = mock(Merchant.class);
        RunMileWallet wallet = mock(RunMileWallet.class);
        Payment payment = mock(Payment.class);
        PaymentRequest request = new PaymentRequest(1L, 10L, 35_000L, 10_000L);

        when(runnerRepository.findById(1L)).thenReturn(Optional.of(runner));
        when(merchantRepository.findById(10L)).thenReturn(Optional.of(merchant));
        when(merchant.isRunmileEnabled()).thenReturn(true);
        when(walletRepository.findByRunnerIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(wallet.getBalance()).thenReturn(10_000L);
        when(paymentGatewayPort.approve(1L, 10L, 35_000L, 25_000L))
                .thenReturn(new PaymentApproval(PaymentStatus.SUCCESS));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(payment.getId()).thenReturn(100L);
        when(payment.getTotalAmount()).thenReturn(35_000L);
        when(payment.getRunmileAmount()).thenReturn(10_000L);
        when(payment.getPersonalAmount()).thenReturn(25_000L);
        when(payment.getStatus()).thenReturn(PaymentStatus.SUCCESS);

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response).isEqualTo(new PaymentResponse(
                100L,
                35_000L,
                10_000L,
                25_000L,
                PaymentStatus.SUCCESS
        ));
        verify(wallet).use(10_000L);
        verify(transactionRepository).save(any(RunMileTransaction.class));
    }

    @Test
    void 잔액이_부족하면_결제를_거절한다() {
        Runner runner = mock(Runner.class);
        Merchant merchant = mock(Merchant.class);
        RunMileWallet wallet = mock(RunMileWallet.class);
        PaymentRequest request = new PaymentRequest(1L, 10L, 35_000L, 10_000L);
        when(runnerRepository.findById(1L)).thenReturn(Optional.of(runner));
        when(merchantRepository.findById(10L)).thenReturn(Optional.of(merchant));
        when(merchant.isRunmileEnabled()).thenReturn(true);
        when(walletRepository.findByRunnerIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(wallet.getBalance()).thenReturn(0L);

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.code()).isEqualTo("INSUFFICIENT_RUNMILE")
                );
    }
}
