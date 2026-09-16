package com.runmile.merchant.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {
    @GetMapping
    public List<Map<String, Object>> getMerchants(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean runmileEnabled
    ) {
        return List.of(Map.of(
                "id", 10,
                "merchantCode", "MERCHANT_00010",
                "name", "RunMile 식당",
                "district", district == null ? "수성구" : district,
                "category", category == null ? "RESTAURANT" : category,
                "address", "대구광역시 수성구",
                "latitude", 35.84,
                "longitude", 128.68,
                "runmileEnabled", runmileEnabled == null || runmileEnabled
        ));
    }
}
