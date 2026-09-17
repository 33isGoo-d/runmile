package com.runmile.payment.domain;

import com.runmile.global.type.PaymentStatus;
import com.runmile.merchant.domain.Merchant;
import com.runmile.runner.domain.Runner;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "runner_id", nullable = false)
    private Runner runner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Column(name = "runmile_amount", nullable = false)
    private long runmileAmount;

    @Column(name = "personal_amount", nullable = false)
    private long personalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status;

    @CreationTimestamp
    @Column(name = "paid_at", nullable = false, updatable = false)
    private Instant paidAt;

    protected Payment() {
    }

    private Payment(
            Runner runner,
            Merchant merchant,
            long totalAmount,
            long runmileAmount,
            long personalAmount,
            PaymentStatus status
    ) {
        this.runner = runner;
        this.merchant = merchant;
        this.totalAmount = totalAmount;
        this.runmileAmount = runmileAmount;
        this.personalAmount = personalAmount;
        this.status = status;
    }

    public static Payment success(
            Runner runner,
            Merchant merchant,
            long totalAmount,
            long runmileAmount,
            long personalAmount
    ) {
        return new Payment(
                runner,
                merchant,
                totalAmount,
                runmileAmount,
                personalAmount,
                PaymentStatus.SUCCESS
        );
    }

    public Long getId() {
        return id;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public long getRunmileAmount() {
        return runmileAmount;
    }

    public long getPersonalAmount() {
        return personalAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
