package com.runmile.wallet.domain;

import com.runmile.global.type.RunMileTransactionType;
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
@Table(name = "runmile_transaction")
public class RunMileTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private RunMileWallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RunMileTransactionType type;

    @Column(nullable = false)
    private long amount;

    @Column(name = "payment_id")
    private Long paymentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RunMileTransaction() {
    }

    private RunMileTransaction(
            RunMileWallet wallet,
            RunMileTransactionType type,
            long amount,
            Long paymentId
    ) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.paymentId = paymentId;
    }

    public static RunMileTransaction issue(RunMileWallet wallet, long amount) {
        return new RunMileTransaction(wallet, RunMileTransactionType.ISSUE, amount, null);
    }

    public static RunMileTransaction use(RunMileWallet wallet, long amount, Long paymentId) {
        return new RunMileTransaction(wallet, RunMileTransactionType.USE, amount, paymentId);
    }

    public Long getId() {
        return id;
    }

    public RunMileTransactionType getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
