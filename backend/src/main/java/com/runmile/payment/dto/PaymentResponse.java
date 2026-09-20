package com.runmile.payment.dto;

import com.runmile.global.type.PaymentStatus;
import com.runmile.payment.domain.Payment;
import java.time.Instant;

public record PaymentResponse(
        Long paymentId,
        Long merchantId,
        String merchantName,
        long totalAmount,
        long runmileAmount,
        long personalAmount,
        PaymentStatus status,
        Instant paidAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMerchant().getId(),
                payment.getMerchant().getName(),
                payment.getTotalAmount(),
                payment.getRunmileAmount(),
                payment.getPersonalAmount(),
                payment.getStatus(),
                payment.getPaidAt()
        );
    }
}
