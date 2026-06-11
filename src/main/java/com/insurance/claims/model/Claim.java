package com.insurance.claims.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
public class Claim {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Business reference shown to users, e.g. CLM001
    @Column(name = "reference", unique = true)
    private String reference;

    @NotBlank(message = "Policy number is required")
    @Column(name = "policy_number", nullable = false)
    private String policyNumber;
    
    @NotBlank(message = "Claimant name is required")
    @Column(name = "claimant_name", nullable = false)
    private String claimantName;
    
    @NotBlank(message = "Claim description is required")
    @Column(name = "claim_description", nullable = false, columnDefinition = "TEXT")
    private String claimDescription;
    
    @NotNull(message = "Claim amount is required")
    @Column(name = "claim_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal claimAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClaimStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private ClaimState state;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "assigned_to")
    private String assignedTo;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    public Claim() {
        this.createdAt = LocalDateTime.now();
        this.status = ClaimStatus.PENDING;
        this.state = ClaimState.SUBMITTED;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }
    
    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }
    
    public String getClaimantName() {
        return claimantName;
    }
    
    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }
    
    public String getClaimDescription() {
        return claimDescription;
    }
    
    public void setClaimDescription(String claimDescription) {
        this.claimDescription = claimDescription;
    }
    
    public BigDecimal getClaimAmount() {
        return claimAmount;
    }
    
    public void setClaimAmount(BigDecimal claimAmount) {
        this.claimAmount = claimAmount;
    }
    
    public ClaimStatus getStatus() {
        return status;
    }
    
    public void setStatus(ClaimStatus status) {
        this.status = status;
    }
    
    public ClaimState getState() {
        return state;
    }
    
    public void setState(ClaimState state) {
        this.state = state;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}