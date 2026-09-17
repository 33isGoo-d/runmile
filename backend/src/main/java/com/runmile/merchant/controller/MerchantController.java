package com.runmile.merchant.controller;

import com.runmile.global.type.MerchantCategory;
import com.runmile.merchant.dto.MerchantResponse;
import com.runmile.merchant.service.MerchantService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {
    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping
    public List<MerchantResponse> getMerchants(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) MerchantCategory category,
            @RequestParam(required = false) Boolean runmileEnabled
    ) {
        return merchantService.getMerchants(district, category, runmileEnabled);
    }
}
