package com.runmile.global.type;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SharedEnumContractTests {
    @Test
    void enumValuesMatchTheSharedContract() {
        assertThat(names(Course.values())).containsExactly("FULL", "TEN_K", "FIVE_K");
        assertThat(names(RunMileTransactionType.values())).containsExactly("ISSUE", "USE", "CANCEL");
        assertThat(names(PaymentStatus.values())).containsExactly("SUCCESS", "CANCELLED");
        assertThat(names(Scenario.values())).containsExactly("NONE", "LOW", "MEDIUM", "HIGH");
        assertThat(names(MerchantCategory.values()))
                .containsExactly("RESTAURANT", "CAFE", "RETAIL", "ACCOMMODATION", "OTHER");
    }

    private String[] names(Enum<?>[] values) {
        return Arrays.stream(values)
                .map(Enum::name)
                .toArray(String[]::new);
    }
}
