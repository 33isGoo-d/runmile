package com.runmile.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.runmile.analytics.dto.DistrictAnalyticsResponse;
import com.runmile.analytics.domain.PolicyEffect;
import com.runmile.analytics.repository.PolicyEffectRepository;
import com.runmile.global.type.PaymentStatus;
import com.runmile.global.type.Scenario;
import com.runmile.merchant.domain.Merchant;
import com.runmile.payment.domain.Payment;
import com.runmile.payment.repository.PaymentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AnalyticsServiceTests {
    @Test
    void 실제_결제_표본이_부족하면_9개_구군_데모_데이터를_반환한다() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PolicyEffectRepository policyEffectRepository = mock(PolicyEffectRepository.class);
        Payment payment = mock(Payment.class);
        Merchant merchant = mock(Merchant.class);
        PolicyEffect effect = mock(PolicyEffect.class);
        when(paymentRepository.findAllByStatusAndRunmileAmountGreaterThan(
                PaymentStatus.SUCCESS,
                0L
        )).thenReturn(List.of(payment));
        when(payment.getMerchant()).thenReturn(merchant);
        when(merchant.getId()).thenReturn(10L);
        when(merchant.getDistrict()).thenReturn("수성구");
        when(payment.getRunmileAmount()).thenReturn(10_000L);
        when(payment.getTotalAmount()).thenReturn(35_000L);
        when(payment.getPersonalAmount()).thenReturn(25_000L);
        when(policyEffectRepository.findFirstByScenarioAndScopeTypeAndScopeValueOrderByIdDesc(
                Scenario.MEDIUM,
                "TOTAL",
                "ALL"
        )).thenReturn(Optional.of(effect));
        when(effect.getScenario()).thenReturn(Scenario.MEDIUM);
        when(effect.getRunmileUsed()).thenReturn(20_187_158L);
        when(effect.getLinkedPaymentAmount()).thenReturn(70_655_050L);
        AnalyticsService service = new AnalyticsService(
                paymentRepository,
                policyEffectRepository
        );

        List<DistrictAnalyticsResponse> result = service.getDistricts(Scenario.MEDIUM);

        assertThat(result).hasSize(9);
        assertThat(result).extracting(DistrictAnalyticsResponse::district)
                .containsExactly(
                        "수성구", "중구", "달서구", "북구", "동구",
                        "남구", "서구", "달성군", "군위군"
                );
        assertThat(result.stream().mapToLong(DistrictAnalyticsResponse::runmileUsed).sum())
                .isEqualTo(20_187_158L);
        assertThat(result.stream().mapToLong(DistrictAnalyticsResponse::linkedPaymentAmount).sum())
                .isEqualTo(70_655_050L);
        assertThat(result).allSatisfy(district -> assertThat(district.personalPaymentAmount())
                .isEqualTo(district.linkedPaymentAmount() - district.runmileUsed()));
        verify(paymentRepository).findAllByStatusAndRunmileAmountGreaterThan(
                PaymentStatus.SUCCESS,
                0L
        );
    }
}
