package com.runmile.completion.domain;

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

@Entity
@Table(name = "nft_record")
public class NftRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "runner_id", nullable = false, unique = true)
    private Runner runner;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "completion_id", nullable = false, unique = true)
    private Completion completion;

    @Column(name = "nft_token_id", nullable = false, unique = true, length = 128)
    private String nftTokenId;

    @Column(nullable = false, length = 64)
    private String network;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "issued_at")
    private Instant issuedAt;

    protected NftRecord() {
    }

    public Long getId() {
        return id;
    }

    public Runner getRunner() {
        return runner;
    }

    public Completion getCompletion() {
        return completion;
    }

    public String getNftTokenId() {
        return nftTokenId;
    }

    public String getNetwork() {
        return network;
    }

    public boolean isVerified() {
        return verified;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
