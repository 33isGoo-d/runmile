package com.runmile.payment.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final RunnerRepository runnerRepository;
    private final MerchantRepository merchantRepository;
    private final RunMileWalletRepository walletRepository;
    private final PaymentRepository paymentRepository;
    private final RunMileTransactionRepository transactionRepository;
    private final PaymentGatewayPort paymentGatewayPort;

    public PaymentService(
            RunnerRepository runnerRepository,
            MerchantRepository merchantRepository,
            RunMileWalletRepository walletRepository,
            PaymentRepository paymentRepository,
            RunMileTransactionRepository transactionRepository,
            PaymentGatewayPort paymentGatewayPort
    ) {
        this.runnerRepository = runnerRepository;
        this.merchantRepository = merchantRepository;
        this.walletRepository = walletRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.paymentGatewayPort = paymentGatewayPort;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        if (request.runmileAmount() > request.totalAmount()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PAYMENT_AMOUNT",
                    "RunMile 사용액은 결제 총액을 초과할 수 없습니다."
            );
        }

        Runner runner = runnerRepository.findById(request.runnerId())
                .orElseThrow(() -> notFound("RUNNER_NOT_FOUND", "참가자를 찾을 수 없습니다."));
        Merchant merchant = merchantRepository.findById(request.merchantId())
                .orElseThrow(() -> notFound("MERCHANT_NOT_FOUND", "가맹점을 찾을 수 없습니다."));
        if (!merchant.isRunmileEnabled()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "RUNMILE_NOT_AVAILABLE",
                    "RunMile을 사용할 수 없는 가맹점입니다."
            );
        }

        RunMileWallet wallet = walletRepository.findByRunnerIdForUpdate(request.runnerId())
                .orElseThrow(() -> notFound("WALLET_NOT_FOUND", "RunMile 지갑을 찾을 수 없습니다."));
        if (wallet.getBalance() < request.runmileAmount()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "INSUFFICIENT_RUNMILE",
                    "RunMile 잔액이 부족합니다."
            );
        }

        long personalAmount = request.personalAmount();
        PaymentApproval approval = paymentGatewayPort.approve(
                request.runnerId(),
                request.merchantId(),
                request.totalAmount(),
                personalAmount
        );
        if (approval.status() != PaymentStatus.SUCCESS) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "PAYMENT_REJECTED",
                    "Mock 결제가 승인되지 않았습니다."
            );
        }

        Payment payment = paymentRepository.save(Payment.success(
                runner,
                merchant,
                request.totalAmount(),
                request.runmileAmount(),
                personalAmount
        ));
        if (request.runmileAmount() > 0) {
            wallet.use(request.runmileAmount());
            transactionRepository.save(RunMileTransaction.use(
                    wallet,
                    request.runmileAmount(),
                    payment.getId()
            ));
        }
        return PaymentResponse.from(payment);
    }

    private ApiException notFound(String code, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, code, message);
    }
}
