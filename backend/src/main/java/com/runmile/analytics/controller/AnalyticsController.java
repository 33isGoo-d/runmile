package com.runmile.analytics.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/analytics")
public class AnalyticsController {
    @GetMapping("/overview")
    public Map<String, Object> overview() {
        return Map.of(
                "scenario", "MEDIUM",
                "runmileBudget", 25000000,
                "runmileUsed", 23100000,
                "linkedPaymentAmount", 74200000,
                "estimatedIncrementalSales", 37400000,
                "effectRatio", 1.50
        );
    }

    @GetMapping("/districts")
    public List<Map<String, Object>> districts() {
        return List.of(Map.of(
                "district", "중구",
                "runmileUsed", 8300000,
                "linkedPaymentAmount", 25100000,
                "personalPaymentAmount", 16800000,
                "transactionCount", 921,
                "merchantCount", 84
        ));
    }

    @GetMapping("/categories")
    public List<Map<String, Object>> categories() {
        return List.of(Map.of(
                "category", "RESTAURANT",
                "runmileUsed", 9100000,
                "linkedPaymentAmount", 28600000,
                "transactionCount", 1034
        ));
    }

    @GetMapping("/effects")
    public List<Map<String, Object>> effects() {
        return List.of(Map.of(
                "scopeType", "DISTRICT",
                "scopeValue", "중구",
                "actualSales", 140000000,
                "predictedBaseline", 112000000,
                "estimatedIncrementalSales", 23000000,
                "effectRatio", 1.21
        ));
    }

    @GetMapping("/insights")
    public List<Map<String, Object>> insights() {
        return List.of(
                Map.of(
                        "type", "CONCENTRATION",
                        "title", "소비 집중",
                        "description", "중구와 수성구에 전체 RunMile 소비의 52%가 집중되었습니다."
                ),
                Map.of(
                        "type", "LOW_USAGE",
                        "title", "저사용 지역",
                        "description", "서구의 RunMile 사용률이 전체 평균보다 낮습니다."
                )
        );
    }
}
