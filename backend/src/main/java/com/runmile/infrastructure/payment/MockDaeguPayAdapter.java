package com.runmile.infrastructure.payment;

import org.springframework.stereotype.Component;

@Component
public class MockDaeguPayAdapter implements PaymentGatewayPort {
    @Override
    public PaymentApproval approve(Long runnerId, Long merchantId, long totalAmount, long personalAmount) {
        return new PaymentApproval("SUCCESS");
    }
}
