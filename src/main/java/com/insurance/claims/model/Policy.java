package com.insurance.claims.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_number", nullable = false, unique = true)
    private String policyNumber;

    @Column(name = "policy_holder", nullable = false)
    private String policyHolder;

    @Column(name = "coverage_type", nullable = false)
    private String coverageType;

    @Column(name = "coverage_amount", precision = 12, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "premium", precision = 10, scale = 2)
    private BigDecimal premium;

    // Fraction of a claim that is covered, e.g. 0.95 for 95%.
    @Column(name = "coverage_percentage", precision = 4, scale = 2)
    private BigDecimal coveragePercentage;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "status")
    private String status;

    public Policy() {
    }

    public Policy(String policyNumber, String policyHolder, String coverageType, BigDecimal coverageAmount,
                  BigDecimal premium, BigDecimal coveragePercentage, LocalDate effectiveDate,
                  LocalDate expiryDate, String status) {
        this.policyNumber = policyNumber;
        this.policyHolder = policyHolder;
        this.coverageType = coverageType;
        this.coverageAmount = coverageAmount;
        this.premium = premium;
        this.coveragePercentage = coveragePercentage;
        this.effectiveDate = effectiveDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPolicyHolder() {
        return policyHolder;
    }

    public void setPolicyHolder(String policyHolder) {
        this.policyHolder = policyHolder;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public void setCoverageType(String coverageType) {
        this.coverageType = coverageType;
    }

    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public BigDecimal getCoveragePercentage() {
        return coveragePercentage;
    }

    public void setCoveragePercentage(BigDecimal coveragePercentage) {
        this.coveragePercentage = coveragePercentage;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
