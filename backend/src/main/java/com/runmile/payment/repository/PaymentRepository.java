package com.runmile.payment.repository;

import com.runmile.global.type.PaymentStatus;
import com.runmile.payment.domain.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @EntityGraph(attributePaths = "merchant")
    List<Payment> findAllByStatusAndRunmileAmountGreaterThan(PaymentStatus status, long runmileAmount);

    @EntityGraph(attributePaths = "merchant")
    List<Payment> findAllByRunnerIdOrderByPaidAtDesc(Long runnerId);

    void deleteAllByRunnerId(Long runnerId);
}
