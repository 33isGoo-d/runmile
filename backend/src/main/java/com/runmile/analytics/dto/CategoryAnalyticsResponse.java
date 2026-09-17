package com.runmile.analytics.dto;

import com.runmile.global.type.MerchantCategory;

public record CategoryAnalyticsResponse(
        MerchantCategory category,
        long runmileUsed,
        long linkedPaymentAmount,
        long transactionCount
) {
}
