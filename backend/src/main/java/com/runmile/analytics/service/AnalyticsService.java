package com.runmile.analytics.service;

import com.runmile.analytics.domain.PolicyEffect;
import com.runmile.analytics.dto.AnalyticsOverviewResponse;
import com.runmile.analytics.dto.CategoryAnalyticsResponse;
import com.runmile.analytics.dto.DistrictAnalyticsResponse;
import com.runmile.analytics.dto.InsightResponse;
import com.runmile.analytics.dto.PolicyEffectResponse;
import com.runmile.analytics.repository.PolicyEffectRepository;
import com.runmile.global.ApiException;
import com.runmile.global.type.MerchantCategory;
import com.runmile.global.type.PaymentStatus;
import com.runmile.global.type.Scenario;
import com.runmile.payment.domain.Payment;
import com.runmile.payment.repository.PaymentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private static final List<DemoDistrict> DEMO_DISTRICTS = List.of(
            new DemoDistrict("수성구", 1_800, 55),
            new DemoDistrict("중구", 1_500, 48),
            new DemoDistrict("달서구", 1_450, 52),
            new DemoDistrict("북구", 1_200, 44),
            new DemoDistrict("동구", 1_100, 42),
            new DemoDistrict("남구", 900, 36),
            new DemoDistrict("서구", 800, 32),
            new DemoDistrict("달성군", 750, 28),
            new DemoDistrict("군위군", 500, 16)
    );
    private static final int TOTAL_WEIGHT = 10_000;

    private final PaymentRepository paymentRepository;
    private final PolicyEffectRepository policyEffectRepository;

    public AnalyticsService(
            PaymentRepository paymentRepository,
            PolicyEffectRepository policyEffectRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.policyEffectRepository = policyEffectRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsOverviewResponse getOverview(Scenario scenario) {
        PolicyEffect effect = policyEffectRepository
                .findFirstByScenarioAndScopeTypeAndScopeValueOrderByIdDesc(
                        scenario,
                        "TOTAL",
                        "ALL"
                )
                .orElseThrow(this::analyticsNotReady);
        return AnalyticsOverviewResponse.from(effect);
    }

    @Transactional(readOnly = true)
    public List<DistrictAnalyticsResponse> getDistricts(Scenario scenario) {
        Map<String, List<Payment>> grouped = successfulPayments().stream()
                .collect(Collectors.groupingBy(
                        payment -> payment.getMerchant().getDistrict(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<DistrictAnalyticsResponse> actualDistricts = grouped.entrySet().stream()
                .map(entry -> toDistrict(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(DistrictAnalyticsResponse::runmileUsed).reversed())
                .toList();
        AnalyticsOverviewResponse overview = getOverview(scenario);

        if (isCompleteActualDataset(actualDistricts, overview)) {
            return actualDistricts;
        }
        return createDemoDistricts(overview);
    }

    @Transactional(readOnly = true)
    public List<CategoryAnalyticsResponse> getCategories() {
        Map<MerchantCategory, List<Payment>> grouped = successfulPayments().stream()
                .collect(Collectors.groupingBy(
                        payment -> payment.getMerchant().getCategory(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(entry -> new CategoryAnalyticsResponse(
                        entry.getKey(),
                        sumRunMile(entry.getValue()),
                        sumTotal(entry.getValue()),
                        entry.getValue().size()
                ))
                .sorted(Comparator.comparingLong(CategoryAnalyticsResponse::runmileUsed).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PolicyEffectResponse> getEffects(Scenario scenario) {
        return policyEffectRepository
                .findAllByScenarioAndScopeTypeOrderByEstimatedIncrementalSalesDesc(
                        scenario,
                        "DISTRICT"
                ).stream()
                .map(PolicyEffectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InsightResponse> getInsights(Scenario scenario) {
        List<DistrictAnalyticsResponse> districts = getDistricts(scenario);
        if (districts.isEmpty()) {
            return List.of(new InsightResponse(
                    "NO_DATA",
                    "결제 데이터 없음",
                    "아직 RunMile 결제 데이터가 없습니다."
            ));
        }

        long total = districts.stream()
                .mapToLong(DistrictAnalyticsResponse::runmileUsed)
                .sum();
        List<DistrictAnalyticsResponse> top = districts.stream().limit(2).toList();
        long topAmount = top.stream().mapToLong(DistrictAnalyticsResponse::runmileUsed).sum();
        long share = total == 0 ? 0 : Math.round(topAmount * 100.0 / total);
        String topNames = top.stream()
                .map(DistrictAnalyticsResponse::district)
                .collect(Collectors.joining("와 "));
        DistrictAnalyticsResponse lowest = districts.stream()
                .min(Comparator.comparingLong(DistrictAnalyticsResponse::runmileUsed))
                .orElseThrow();

        List<InsightResponse> insights = new ArrayList<>();
        insights.add(new InsightResponse(
                "CONCENTRATION",
                "소비 집중",
                topNames + "에 전체 RunMile 소비의 " + share + "%가 집중되었습니다."
        ));
        insights.add(new InsightResponse(
                "LOW_USAGE",
                "저사용 지역",
                lowest.district() + "의 RunMile 사용액이 가장 낮습니다."
        ));
        return insights;
    }

    private List<Payment> successfulPayments() {
        return paymentRepository.findAllByStatusAndRunmileAmountGreaterThan(
                PaymentStatus.SUCCESS,
                0L
        );
    }

    private DistrictAnalyticsResponse toDistrict(String district, List<Payment> payments) {
        Set<Long> merchantIds = payments.stream()
                .map(payment -> payment.getMerchant().getId())
                .collect(Collectors.toSet());
        return new DistrictAnalyticsResponse(
                district,
                sumRunMile(payments),
                sumTotal(payments),
                payments.stream().mapToLong(Payment::getPersonalAmount).sum(),
                payments.size(),
                merchantIds.size()
        );
    }

    private boolean isCompleteActualDataset(
            List<DistrictAnalyticsResponse> districts,
            AnalyticsOverviewResponse overview
    ) {
        Set<String> actualNames = districts.stream()
                .map(DistrictAnalyticsResponse::district)
                .collect(Collectors.toSet());
        Set<String> expectedNames = DEMO_DISTRICTS.stream()
                .map(DemoDistrict::name)
                .collect(Collectors.toSet());
        long runmileTotal = districts.stream()
                .mapToLong(DistrictAnalyticsResponse::runmileUsed)
                .sum();
        long linkedPaymentTotal = districts.stream()
                .mapToLong(DistrictAnalyticsResponse::linkedPaymentAmount)
                .sum();
        return actualNames.equals(expectedNames)
                && runmileTotal == overview.runmileUsed()
                && linkedPaymentTotal == overview.linkedPaymentAmount();
    }

    private List<DistrictAnalyticsResponse> createDemoDistricts(
            AnalyticsOverviewResponse overview
    ) {
        List<Long> runmileAmounts = allocate(overview.runmileUsed());
        List<Long> linkedPaymentAmounts = allocate(overview.linkedPaymentAmount());
        List<DistrictAnalyticsResponse> result = new ArrayList<>();

        for (int index = 0; index < DEMO_DISTRICTS.size(); index++) {
            DemoDistrict district = DEMO_DISTRICTS.get(index);
            long runmileAmount = runmileAmounts.get(index);
            long linkedPaymentAmount = linkedPaymentAmounts.get(index);
            result.add(new DistrictAnalyticsResponse(
                    district.name(),
                    runmileAmount,
                    linkedPaymentAmount,
                    linkedPaymentAmount - runmileAmount,
                    Math.max(1, Math.round(linkedPaymentAmount / 35_000.0)),
                    district.merchantCount()
            ));
        }
        return result;
    }

    private List<Long> allocate(long total) {
        List<Long> amounts = new ArrayList<>();
        long allocated = 0;
        for (int index = 0; index < DEMO_DISTRICTS.size(); index++) {
            long amount = index == DEMO_DISTRICTS.size() - 1
                    ? total - allocated
                    : total * DEMO_DISTRICTS.get(index).weight() / TOTAL_WEIGHT;
            amounts.add(amount);
            allocated += amount;
        }
        return amounts;
    }

    private long sumRunMile(List<Payment> payments) {
        return payments.stream().mapToLong(Payment::getRunmileAmount).sum();
    }

    private long sumTotal(List<Payment> payments) {
        return payments.stream().mapToLong(Payment::getTotalAmount).sum();
    }

    private ApiException analyticsNotReady() {
        return new ApiException(
                HttpStatus.NOT_FOUND,
                "ANALYTICS_NOT_READY",
                "AI 분석 결과가 아직 적재되지 않았습니다."
        );
    }

    private record DemoDistrict(String name, int weight, long merchantCount) {
    }
}
