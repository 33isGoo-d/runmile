package com.runmile.merchant.service;

import com.runmile.global.type.MerchantCategory;
import com.runmile.merchant.dto.MerchantResponse;
import com.runmile.merchant.repository.MerchantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {
    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Transactional(readOnly = true)
    public List<MerchantResponse> getMerchants(
            String district,
            MerchantCategory category,
            Boolean runmileEnabled
    ) {
        return merchantRepository.findByFilters(district, category, runmileEnabled).stream()
                .map(MerchantResponse::from)
                .toList();
    }
}
