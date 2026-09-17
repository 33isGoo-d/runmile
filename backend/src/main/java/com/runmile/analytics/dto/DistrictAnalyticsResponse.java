package com.runmile.analytics.dto;

public record DistrictAnalyticsResponse(
        String district,
        long runmileUsed,
        long linkedPaymentAmount,
        long personalPaymentAmount,
        long transactionCount,
        long merchantCount
) {
}
