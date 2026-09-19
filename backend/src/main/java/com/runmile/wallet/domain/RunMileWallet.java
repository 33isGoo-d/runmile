package com.runmile.wallet.domain;

import com.runmile.runner.domain.Runner;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "runmile_wallet")
public class RunMileWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "runner_id", nullable = false, unique = true)
    private Runner runner;

    @Column(nullable = false)
    private long balance;

    @Column(name = "total_issued", nullable = false)
    private long totalIssued;

    @Column(name = "total_used", nullable = false)
    private long totalUsed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RunMileWallet() {
    }

    public void issue(long amount) {
        balance += amount;
        totalIssued += amount;
    }

    public void use(long amount) {
        balance -= amount;
        totalUsed += amount;
    }

    public void resetDemoState() {
        balance = 0;
        totalIssued = 0;
        totalUsed = 0;
    }

    public Long getId() {
        return id;
    }

    public Runner getRunner() {
        return runner;
    }

    public long getBalance() {
        return balance;
    }

    public long getTotalIssued() {
        return totalIssued;
    }

    public long getTotalUsed() {
        return totalUsed;
    }
}
