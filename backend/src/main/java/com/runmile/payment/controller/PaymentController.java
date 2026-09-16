package com.runmile.payment.controller;

import com.runmile.global.ApiException;
import com.runmile.infrastructure.payment.PaymentApproval;
import com.runmile.infrastructure.payment.PaymentGatewayPort;
import com.runmile.payment.dto.PaymentRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentGatewayPort paymentGatewayPort;

    public PaymentController(PaymentGatewayPort paymentGatewayPort) {
        this.paymentGatewayPort = paymentGatewayPort;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createPayment(@RequestBody PaymentRequest request) {
        if (request.totalAmount() <= 0) {
            throw new IllegalArgumentException("totalAmount must be greater than 0");
        }
        if (request.runmileAmount() < 0 || request.runmileAmount() > request.totalAmount()) {
            throw new IllegalArgumentException("runmileAmount must be between 0 and totalAmount");
        }
        if (request.runmileAmount() > 10000) {
            throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_RUNMILE", "RunMile 잔액이 부족합니다.");
        }

        PaymentApproval approval = paymentGatewayPort.approve(
                request.runnerId(),
                request.merchantId(),
                request.totalAmount(),
                request.personalAmount()
        );

        return Map.of(
                "paymentId", 100,
                "totalAmount", request.totalAmount(),
                "runmileAmount", request.runmileAmount(),
                "personalAmount", request.personalAmount(),
                "status", approval.status()
        );
    }
}
