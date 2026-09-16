package com.runmile.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PaymentRequest(
        @NotNull Long runnerId,
        @NotNull Long merchantId,
        @NotNull @Positive Long totalAmount,
        @NotNull @PositiveOrZero Long runmileAmount
) {
    public long personalAmount() {
        return totalAmount - runmileAmount;
    }
}
