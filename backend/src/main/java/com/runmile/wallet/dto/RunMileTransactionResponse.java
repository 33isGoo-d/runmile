package com.runmile.wallet.dto;

import com.runmile.global.type.RunMileTransactionType;
import com.runmile.wallet.domain.RunMileTransaction;
import java.time.Instant;

public record RunMileTransactionResponse(
        Long id,
        RunMileTransactionType type,
        long amount,
        Long paymentId,
        Instant createdAt
) {
    public static RunMileTransactionResponse from(RunMileTransaction transaction) {
        return new RunMileTransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getPaymentId(),
                transaction.getCreatedAt()
        );
    }
}
