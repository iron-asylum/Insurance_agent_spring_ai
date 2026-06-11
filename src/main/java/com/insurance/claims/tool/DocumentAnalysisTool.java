package com.insurance.claims.tool;

import com.insurance.claims.model.ClaimDocument;
import com.insurance.claims.repository.ClaimDocumentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DocumentAnalysisTool implements AgentTool {

    private final ClaimDocumentRepository documentRepository;

    public DocumentAnalysisTool(ClaimDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public String name() {
        return "analyze_documents";
    }

    @Override
    public String description() {
        return "Analyze the supporting documents for a claim and extract a summary, key findings, evidence type, relevance score, confidence level and risk assessment.";
    }

    @Override
    public List<ToolParam> parameters() {
        return List.of(
                new ToolParam("claimId", "The claim reference to analyze, e.g. CLM001", true)
        );
    }

    @Override
    public String execute(Map<String, String> args) {
        return analyzeDocuments(args.get("claimId"));
    }

    public String analyzeDocuments(String claimId) {
        if (claimId == null || claimId.isBlank()) {
            return "Error: claimId is required.";
        }
        Optional<ClaimDocument> found = documentRepository.findByClaimReference(claimId.trim());
        if (found.isEmpty()) {
            return "No document analysis data found for claim: " + claimId;
        }
        ClaimDocument doc = found.get();

        StringBuilder result = new StringBuilder();
        result.append("Document Analysis for Claim ").append(doc.getClaimReference()).append("\n");
        result.append("========================================\n");
        result.append("Summary: ").append(doc.getSummary()).append("\n");
        result.append("Key Findings: ").append(doc.getKeyFindings()).append("\n");
        result.append("Evidence Type: ").append(doc.getEvidenceType()).append("\n");
        result.append("Relevance Score: ").append(doc.getRelevanceScore()).append("\n");
        result.append("Confidence Level: ").append(doc.getConfidenceLevel()).append("\n");
        result.append("Risk Assessment: ").append(doc.getRiskAssessment()).append("\n");

        return result.toString();
    }
}
