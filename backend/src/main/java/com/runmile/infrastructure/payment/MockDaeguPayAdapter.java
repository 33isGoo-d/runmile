package com.runmile.infrastructure.payment;

import com.runmile.global.type.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class MockDaeguPayAdapter implements PaymentGatewayPort {
    @Override
    public PaymentApproval approve(Long runnerId, Long merchantId, long totalAmount, long personalAmount) {
        return new PaymentApproval(PaymentStatus.SUCCESS);
    }
}
