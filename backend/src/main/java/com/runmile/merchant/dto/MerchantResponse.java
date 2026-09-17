package com.runmile.merchant.dto;

import com.runmile.global.type.MerchantCategory;
import com.runmile.merchant.domain.Merchant;
import java.math.BigDecimal;

public record MerchantResponse(
        Long id,
        String merchantCode,
        String name,
        String district,
        MerchantCategory category,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean runmileEnabled
) {
    public static MerchantResponse from(Merchant merchant) {
        return new MerchantResponse(
                merchant.getId(),
                merchant.getMerchantCode(),
                merchant.getName(),
                merchant.getDistrict(),
                merchant.getCategory(),
                merchant.getAddress(),
                merchant.getLatitude(),
                merchant.getLongitude(),
                merchant.isRunmileEnabled()
        );
    }
}
