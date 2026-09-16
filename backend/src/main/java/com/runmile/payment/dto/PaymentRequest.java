package com.runmile.payment.dto;

public record PaymentRequest(Long runnerId, Long merchantId, long totalAmount, long runmileAmount) {
    public long personalAmount() {
        return totalAmount - runmileAmount;
    }
}

