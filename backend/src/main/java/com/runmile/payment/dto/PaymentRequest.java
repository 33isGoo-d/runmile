package com.runmile.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PaymentRequest(
        @NotNull(message = "참가자 ID는 필수입니다.") Long runnerId,
        @NotNull(message = "가맹점 ID는 필수입니다.") Long merchantId,
        @NotNull(message = "결제 총액은 필수입니다.")
        @Positive(message = "결제 총액은 0보다 커야 합니다.") Long totalAmount,
        @NotNull(message = "RunMile 사용액은 필수입니다.")
        @PositiveOrZero(message = "RunMile 사용액은 0 이상이어야 합니다.") Long runmileAmount
) {
    public long personalAmount() {
        return totalAmount - runmileAmount;
    }
}
