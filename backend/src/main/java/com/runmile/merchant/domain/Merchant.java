package com.runmile.merchant.domain;

import com.runmile.global.type.MerchantCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "merchant")
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_code", nullable = false, unique = true, length = 64)
    private String merchantCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String district;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MerchantCategory category;

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "runmile_enabled", nullable = false)
    private boolean runmileEnabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Merchant() {
    }

    public Long getId() {
        return id;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public MerchantCategory getCategory() {
        return category;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public boolean isRunmileEnabled() {
        return runmileEnabled;
    }
}
