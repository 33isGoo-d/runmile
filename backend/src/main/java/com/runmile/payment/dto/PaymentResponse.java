package com.runmile.payment.dto;

import com.runmile.global.type.PaymentStatus;
import com.runmile.payment.domain.Payment;

public record PaymentResponse(
        Long paymentId,
        long totalAmount,
        long runmileAmount,
        long personalAmount,
        PaymentStatus status
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getTotalAmount(),
                payment.getRunmileAmount(),
                payment.getPersonalAmount(),
                payment.getStatus()
        );
    }
}
