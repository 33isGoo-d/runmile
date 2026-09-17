package com.runmile.payment.repository;

import com.runmile.global.type.PaymentStatus;
import com.runmile.payment.domain.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByStatus(PaymentStatus status);
}
