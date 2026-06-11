package com.insurance.claims.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The result of analyzing the supporting documents for a single claim.
 * One row per claim reference (e.g. CLM001).
 */
@Entity
@Table(name = "claim_documents")
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_reference", nullable = false, unique = true)
    private String claimReference;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "key_findings", columnDefinition = "TEXT")
    private String keyFindings;

    @Column(name = "evidence_type")
    private String evidenceType;

    @Column(name = "relevance_score")
    private String relevanceScore;

    @Column(name = "confidence_level")
    private String confidenceLevel;

    @Column(name = "risk_assessment", columnDefinition = "TEXT")
    private String riskAssessment;

    public ClaimDocument() {
    }

    public ClaimDocument(String claimReference, String summary, String keyFindings, String evidenceType,
                         String relevanceScore, String confidenceLevel, String riskAssessment) {
        this.claimReference = claimReference;
        this.summary = summary;
        this.keyFindings = keyFindings;
        this.evidenceType = evidenceType;
        this.relevanceScore = relevanceScore;
        this.confidenceLevel = confidenceLevel;
        this.riskAssessment = riskAssessment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClaimReference() {
        return claimReference;
    }

    public void setClaimReference(String claimReference) {
        this.claimReference = claimReference;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getKeyFindings() {
        return keyFindings;
    }

    public void setKeyFindings(String keyFindings) {
        this.keyFindings = keyFindings;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(String relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public String getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(String riskAssessment) {
        this.riskAssessment = riskAssessment;
    }
}
