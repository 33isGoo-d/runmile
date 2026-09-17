package com.runmile.wallet.dto;

import com.runmile.global.type.RunMileTransactionType;
import com.runmile.wallet.domain.RunMileTransaction;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record RunMileTransactionResponse(
        Long id,
        RunMileTransactionType type,
        long amount,
        Long paymentId,
        LocalDateTime createdAt
) {
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    public static RunMileTransactionResponse from(RunMileTransaction transaction) {
        return new RunMileTransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getPaymentId(),
                LocalDateTime.ofInstant(transaction.getCreatedAt(), SEOUL_ZONE)
        );
    }
}
