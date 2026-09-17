package com.runmile.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.runmile.analytics.dto.DistrictAnalyticsResponse;
import com.runmile.analytics.repository.PolicyEffectRepository;
import com.runmile.global.type.PaymentStatus;
import com.runmile.merchant.domain.Merchant;
import com.runmile.payment.domain.Payment;
import com.runmile.payment.repository.PaymentRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalyticsServiceTests {
    @Test
    void RunMile을_사용한_성공_결제만_지역_집계에_포함한다() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PolicyEffectRepository policyEffectRepository = mock(PolicyEffectRepository.class);
        Payment payment = mock(Payment.class);
        Merchant merchant = mock(Merchant.class);
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
        AnalyticsService service = new AnalyticsService(
                paymentRepository,
                policyEffectRepository
        );

        List<DistrictAnalyticsResponse> result = service.getDistricts();

        assertThat(result).containsExactly(new DistrictAnalyticsResponse(
                "수성구",
                10_000L,
                35_000L,
                25_000L,
                1,
                1
        ));
        verify(paymentRepository).findAllByStatusAndRunmileAmountGreaterThan(
                PaymentStatus.SUCCESS,
                0L
        );
    }
}
