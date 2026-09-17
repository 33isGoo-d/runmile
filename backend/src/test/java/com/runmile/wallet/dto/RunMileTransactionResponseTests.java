package com.runmile.wallet.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.runmile.global.type.RunMileTransactionType;
import com.runmile.wallet.domain.RunMileTransaction;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RunMileTransactionResponseTests {
    @Test
    void 거래_시각을_타임존_정보가_있는_Instant로_응답한다() {
        Instant createdAt = Instant.parse("2026-09-18T01:00:00Z");
        RunMileTransaction transaction = mock(RunMileTransaction.class);
        when(transaction.getId()).thenReturn(1L);
        when(transaction.getType()).thenReturn(RunMileTransactionType.ISSUE);
        when(transaction.getAmount()).thenReturn(10_000L);
        when(transaction.getCreatedAt()).thenReturn(createdAt);

        RunMileTransactionResponse response = RunMileTransactionResponse.from(transaction);

        assertThat(response.createdAt()).isEqualTo(createdAt);
    }
}
