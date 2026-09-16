package com.runmile.infrastructure.payment;

public interface PaymentGatewayPort {
    PaymentApproval approve(Long runnerId, Long merchantId, long totalAmount, long personalAmount);
}

